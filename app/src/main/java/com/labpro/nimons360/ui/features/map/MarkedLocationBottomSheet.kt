package com.labpro.nimons360.ui.features.map

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.labpro.nimons360.MainApplication
import com.labpro.nimons360.R
import com.labpro.nimons360.data.model.map.FavoriteLocationEntity
import java.io.File
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MarkedLocationBottomSheet : BottomSheetDialogFragment() {
    interface Listener {
        fun onSaveMarkedLocation(location: FavoriteLocationEntity)
        fun onDeleteMarkedLocation(location: FavoriteLocationEntity)
    }

    private lateinit var title: TextView
    private lateinit var name: TextInputEditText
    private lateinit var description: TextInputEditText
    private lateinit var latitude: TextInputEditText
    private lateinit var longitude: TextInputEditText
    private lateinit var photoEmpty: TextView
    private lateinit var photoContainer: LinearLayout
    private lateinit var photoActions: View
    private lateinit var useCurrent: MaterialButton
    private lateinit var navigate: MaterialButton
    private lateinit var gallery: MaterialButton
    private lateinit var camera: MaterialButton
    private lateinit var delete: MaterialButton
    private lateinit var edit: MaterialButton
    private lateinit var save: MaterialButton

    private val photoPaths = mutableListOf<String>()
    private val addedPhotoPaths = mutableSetOf<String>()
    private val initialPhotoPaths = mutableSetOf<String>()
    private var pendingCameraPath: String? = null
    private var committed = false
    private var editing = false

    private val repository
        get() = (requireActivity().application as MainApplication).locationRepository

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isEmpty()) return@registerForActivityResult
        viewLifecycleOwner.lifecycleScope.launch {
            val imported = withContext(Dispatchers.IO) {
                uris.mapNotNull { uri ->
                    runCatching { repository.importPhoto(uri) }.getOrNull()
                }
            }
            photoPaths.addAll(imported)
            addedPhotoPaths.addAll(imported)
            renderPhotos()
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val path = pendingCameraPath
        pendingCameraPath = null
        if (success && path != null) {
            photoPaths.add(path)
            addedPhotoPaths.add(path)
            renderPhotos()
        } else if (path != null) {
            repository.deleteUncommittedPhotos(listOf(path))
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openCamera()
        else Toast.makeText(requireContext(), R.string.marked_location_camera_permission, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.bottom_sheet_marked_location, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind(view)
        populate()

        useCurrent.setOnClickListener { useCurrentLocation() }
        navigate.setOnClickListener { openNavigation() }
        gallery.setOnClickListener { galleryLauncher.launch("image/*") }
        camera.setOnClickListener { requestCamera() }
        edit.setOnClickListener { setEditing(true) }
        save.setOnClickListener { save() }
        delete.setOnClickListener { confirmDelete() }
    }

    override fun onDestroy() {
        if (!committed) {
            repository.deleteUncommittedPhotos(addedPhotoPaths)
        }
        super.onDestroy()
    }

    private fun bind(view: View) {
        title = view.findViewById(R.id.tvMarkedLocationTitle)
        name = view.findViewById(R.id.etMarkedName)
        description = view.findViewById(R.id.etMarkedDescription)
        latitude = view.findViewById(R.id.etMarkedLatitude)
        longitude = view.findViewById(R.id.etMarkedLongitude)
        photoEmpty = view.findViewById(R.id.tvMarkedPhotoEmpty)
        photoContainer = view.findViewById(R.id.markedPhotoContainer)
        photoActions = view.findViewById(R.id.markedPhotoActions)
        useCurrent = view.findViewById(R.id.btnUseCurrentLocation)
        navigate = view.findViewById(R.id.btnNavigateMarkedLocation)
        gallery = view.findViewById(R.id.btnMarkedGallery)
        camera = view.findViewById(R.id.btnMarkedCamera)
        delete = view.findViewById(R.id.btnDeleteMarkedLocation)
        edit = view.findViewById(R.id.btnEditMarkedLocation)
        save = view.findViewById(R.id.btnSaveMarkedLocation)
    }

    private fun populate() {
        val id = requireArguments().getInt(ARG_ID)
        name.setText(requireArguments().getString(ARG_NAME).orEmpty())
        description.setText(requireArguments().getString(ARG_DESCRIPTION).orEmpty())
        latitude.setText(formatCoordinate(requireArguments().getDouble(ARG_LATITUDE)))
        longitude.setText(formatCoordinate(requireArguments().getDouble(ARG_LONGITUDE)))
        photoPaths.addAll(requireArguments().getStringArrayList(ARG_PHOTOS).orEmpty())
        initialPhotoPaths.addAll(photoPaths)

        editing = id == 0
        setEditing(editing)
        renderPhotos()
    }

    private fun setEditing(enabled: Boolean) {
        editing = enabled
        val existing = requireArguments().getInt(ARG_ID) != 0
        title.setText(
            when {
                !existing -> R.string.marked_location_add_title
                enabled -> R.string.marked_location_edit_title
                else -> R.string.marked_location_detail_title
            }
        )
        name.isEnabled = enabled
        description.isEnabled = enabled
        latitude.isEnabled = enabled
        longitude.isEnabled = enabled
        photoActions.isVisible = enabled
        useCurrent.isVisible = enabled
        edit.isVisible = existing && !enabled
        delete.isVisible = existing
        save.isVisible = enabled
        renderPhotos()
    }

    private fun renderPhotos() {
        photoContainer.removeAllViews()
        photoEmpty.isVisible = photoPaths.isEmpty()
        photoPaths.forEach { path ->
            val wrapper = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 0, resources.getDimensionPixelSize(R.dimen.spacing_sm), 0)
            }
            val image = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.marked_photo_size),
                    resources.getDimensionPixelSize(R.dimen.marked_photo_size),
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
                contentDescription = getString(R.string.marked_location_photo)
                setImageBitmap(BitmapFactory.decodeFile(path))
            }
            wrapper.addView(image)
            if (editing) {
                wrapper.addView(
                    MaterialButton(
                        requireContext(),
                        null,
                        com.google.android.material.R.attr.materialButtonOutlinedStyle,
                    ).apply {
                        text = getString(R.string.marked_location_remove_photo)
                        setOnClickListener {
                            photoPaths.remove(path)
                            if (addedPhotoPaths.remove(path)) {
                                repository.deleteUncommittedPhotos(listOf(path))
                            }
                            renderPhotos()
                        }
                    }
                )
            }
            photoContainer.addView(wrapper)
        }
    }

    private fun useCurrentLocation() {
        val currentLat = requireArguments().getDouble(ARG_CURRENT_LATITUDE, Double.NaN)
        val currentLon = requireArguments().getDouble(ARG_CURRENT_LONGITUDE, Double.NaN)
        if (currentLat.isNaN() || currentLon.isNaN()) {
            Toast.makeText(requireContext(), R.string.marked_location_current_unavailable, Toast.LENGTH_SHORT).show()
            return
        }
        latitude.setText(formatCoordinate(currentLat))
        longitude.setText(formatCoordinate(currentLon))
    }

    private fun openNavigation() {
        val lat = latitude.text?.toString()?.toDoubleOrNull()
        val lon = longitude.text?.toString()?.toDoubleOrNull()
        if (lat == null || lon == null) {
            Toast.makeText(requireContext(), R.string.marked_location_invalid_coordinates, Toast.LENGTH_SHORT).show()
            return
        }
        val uri = "google.navigation:q=$lat,$lon".toUri()
        val googleMaps = Intent(Intent.ACTION_VIEW, uri).setPackage("com.google.android.apps.maps")
        try {
            startActivity(googleMaps)
        } catch (_: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, "geo:$lat,$lon?q=$lat,$lon".toUri()))
        }
    }

    private fun requestCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        val file = repository.createCameraPhoto()
        pendingCameraPath = file.absolutePath
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file,
        )
        cameraLauncher.launch(uri)
    }

    private fun save() {
        val markedName = name.text?.toString()?.trim().orEmpty()
        val markedDescription = description.text?.toString()?.trim().orEmpty()
        val lat = latitude.text?.toString()?.toDoubleOrNull()
        val lon = longitude.text?.toString()?.toDoubleOrNull()

        if (markedName.isBlank()) {
            name.error = getString(R.string.marked_location_name_required)
            return
        }
        if (markedDescription.isBlank()) {
            description.error = getString(R.string.marked_location_description_required)
            return
        }
        if (lat == null || lon == null || lat !in -90.0..90.0 || lon !in -180.0..180.0) {
            Toast.makeText(requireContext(), R.string.marked_location_invalid_coordinates, Toast.LENGTH_SHORT).show()
            return
        }

        committed = true
        listener()?.onSaveMarkedLocation(
            FavoriteLocationEntity(
                id = requireArguments().getInt(ARG_ID),
                latitude = lat,
                longitude = lon,
                title = markedName,
                description = markedDescription,
                photoPaths = photoPaths.toList(),
            )
        )
        dismiss()
    }

    private fun confirmDelete() {
        val location = currentLocation()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.marked_location_delete_title)
            .setMessage(R.string.marked_location_delete_message)
            .setPositiveButton(R.string.marked_location_delete) { _, _ ->
                repository.deleteUncommittedPhotos(addedPhotoPaths)
                addedPhotoPaths.clear()
                committed = true
                listener()?.onDeleteMarkedLocation(location)
                dismiss()
            }
            .setNegativeButton(R.string.btn_close, null)
            .show()
    }

    private fun currentLocation() = FavoriteLocationEntity(
        id = requireArguments().getInt(ARG_ID),
        latitude = latitude.text?.toString()?.toDoubleOrNull()
            ?: requireArguments().getDouble(ARG_LATITUDE),
        longitude = longitude.text?.toString()?.toDoubleOrNull()
            ?: requireArguments().getDouble(ARG_LONGITUDE),
        title = name.text?.toString().orEmpty(),
        description = description.text?.toString().orEmpty(),
        photoPaths = initialPhotoPaths.toList(),
    )

    private fun listener(): Listener? = parentFragment as? Listener

    private fun formatCoordinate(value: Double): String = String.format(Locale.US, "%.6f", value)

    companion object {
        const val TAG = "MarkedLocationBottomSheet"
        private const val ARG_ID = "id"
        private const val ARG_NAME = "name"
        private const val ARG_DESCRIPTION = "description"
        private const val ARG_LATITUDE = "latitude"
        private const val ARG_LONGITUDE = "longitude"
        private const val ARG_PHOTOS = "photos"
        private const val ARG_CURRENT_LATITUDE = "current_latitude"
        private const val ARG_CURRENT_LONGITUDE = "current_longitude"

        fun newLocation(
            latitude: Double,
            longitude: Double,
            currentLatitude: Double?,
            currentLongitude: Double?,
        ) = create(
            FavoriteLocationEntity(
                latitude = latitude,
                longitude = longitude,
                title = "",
            ),
            currentLatitude,
            currentLongitude,
        )

        fun existingLocation(
            location: FavoriteLocationEntity,
            currentLatitude: Double?,
            currentLongitude: Double?,
        ) = create(location, currentLatitude, currentLongitude)

        private fun create(
            location: FavoriteLocationEntity,
            currentLatitude: Double?,
            currentLongitude: Double?,
        ) = MarkedLocationBottomSheet().apply {
            arguments = Bundle().apply {
                putInt(ARG_ID, location.id)
                putString(ARG_NAME, location.title)
                putString(ARG_DESCRIPTION, location.description)
                putDouble(ARG_LATITUDE, location.latitude)
                putDouble(ARG_LONGITUDE, location.longitude)
                putStringArrayList(ARG_PHOTOS, ArrayList(location.photoPaths))
                currentLatitude?.let { putDouble(ARG_CURRENT_LATITUDE, it) }
                currentLongitude?.let { putDouble(ARG_CURRENT_LONGITUDE, it) }
            }
        }
    }
}
