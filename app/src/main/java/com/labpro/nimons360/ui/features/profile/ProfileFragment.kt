package com.labpro.nimons360.ui.features.profile

import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import coil.load
import com.labpro.nimons360.MainApplication
import com.labpro.nimons360.R
import com.labpro.nimons360.core.utils.TokenManager
import com.labpro.nimons360.viewmodel.ProfileViewModel
import com.labpro.nimons360.viewmodel.ProfileViewModelFactory
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File

class ProfileFragment : DialogFragment() {

    private val viewModel: ProfileViewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            ProfileViewModelFactory(
                (requireActivity().application as MainApplication).userRepository,
                (requireActivity().application as MainApplication).authRepository
            )
        )[ProfileViewModel::class.java]
    }

    private var tvFullName: TextView? = null
    private var tvEmail: TextView? = null
    private var tvAvatarInitials: TextView? = null
    private var ivProfilePhoto: ImageView? = null

    private var tempImageUri: Uri? = null
    private var initialName: String? = null
    private var initialPhotoUrl: String? = null
    private var hasChangedPhoto = false
    private var isUploadingPhoto = false

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            processAndUploadImage(uri)
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            tempImageUri?.let { processAndUploadImage(it) }
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCameraIntent()
        } else {
            Toast.makeText(requireContext(), "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCameraIntent() {
        try {
            tempImageUri = createTempImageUri()
            takePictureLauncher.launch(tempImageUri!!)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Camera capture error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.Theme_Nimons360_ProfileDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvFullName = view.findViewById(R.id.tvFullName)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvAvatarInitials = view.findViewById(R.id.tvAvatarInitials)
        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto)

        view.findViewById<View>(R.id.btnClose).setOnClickListener {
            dismiss()
        }

        setupListeners(view)
        observeState()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        }
    }

    private fun setupListeners(view: View) {
        view.findViewById<View>(R.id.btnSignOut).setOnClickListener {
            showSignOutConfirmation()
        }

        view.findViewById<View>(R.id.btnEditName).setOnClickListener {
            showEditNameBottomSheet()
        }

        view.findViewById<View>(R.id.btnEditPhoto).setOnClickListener {
            showImageSourceOptions()
        }

        val app = requireActivity().application as MainApplication
        val switchLoc = view.findViewById<SwitchMaterial>(R.id.switchShareLocation)
        switchLoc?.isChecked = app.tokenManager.isLocationSharingEnabled()
        switchLoc?.setOnCheckedChangeListener { _, isChecked ->
            // Update location privacy setting in secure preferences
            // If checked is false, location broadcasts are intercepted.
            app.tokenManager.setLocationSharingEnabled(isChecked)
            app.analytics.locationShared(isChecked)
        }

        view.findViewById<View>(R.id.btnCustomizePin).setOnClickListener {
            showPinDialog()
        }

        view.findViewById<View>(R.id.btnAnalytics).setOnClickListener {
            app.analytics.analyticsOpened()
            showAnalyticsDialog()
        }
    }

    private fun showAnalyticsDialog() {
        val app = requireActivity().application as MainApplication
        val summary = app.analytics.summary()
        val sharing = if (app.tokenManager.isLocationSharingEnabled()) {
            getString(R.string.analytics_enabled)
        } else {
            getString(R.string.analytics_disabled)
        }
        val message = if (summary.isEmpty) {
            getString(R.string.analytics_empty_summary, sharing)
        } else {
            getString(
                R.string.analytics_summary,
                summary.mapOpened,
                summary.favoriteAdded,
                summary.favoriteRemoved,
                summary.pinCustomized,
                summary.memberPopupOpened,
                sharing,
            )
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.analytics_title)
            .setMessage(message)
            .setPositiveButton(R.string.analytics_reset) { _, _ ->
                app.analytics.resetSummary()
                Toast.makeText(requireContext(), R.string.analytics_reset_done, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.btn_close, null)
            .show()
    }

    private fun showPinDialog() {
        val app = requireActivity().application as MainApplication
        val styles = arrayOf(
            TokenManager.PIN_TEAL,
            TokenManager.PIN_CORAL,
            TokenManager.PIN_BLUE,
            TokenManager.PIN_PURPLE,
            TokenManager.PIN_ORANGE,
        )
        val labels = arrayOf("Teal", "Coral", "Blue", "Purple", "Orange")
        val current = styles.indexOf(app.tokenManager.getPinStyle()).coerceAtLeast(0)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Customize Pin")
            .setSingleChoiceItems(labels, current) { dialog, which ->
                app.tokenManager.setPinStyle(styles[which])
                app.analytics.pinCustomized(styles[which])
                Toast.makeText(requireContext(), "Pin style updated", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                tvFullName?.text = state.name
                tvEmail?.text = state.email

                tvAvatarInitials?.text =
                    state.name.firstOrNull()?.uppercase() ?: ""

                // Record initial state once fully loaded
                if (initialName == null && !state.isLoading && state.name.isNotEmpty()) {
                    initialName = state.name
                    initialPhotoUrl = state.profileImageUrl
                }

                // If currently performing photo upload and loading finishes with no error, mark photo as changed
                if (isUploadingPhoto && !state.isLoading) {
                    isUploadingPhoto = false
                    if (state.error == null) {
                        hasChangedPhoto = true
                    }
                }

                if (!state.profileImageUrl.isNullOrBlank()) {
                    ivProfilePhoto?.visibility = View.VISIBLE
                    tvAvatarInitials?.visibility = View.GONE
                    val cleanPath = state.profileImageUrl.substringBefore("?")
                    val resolvedUrl = if (cleanPath.startsWith("/")) {
                        "${com.labpro.nimons360.BuildConfig.BASE_URL}$cleanPath"
                    } else {
                        cleanPath
                    }
                    try {
                        coil.Coil.imageLoader(requireContext()).diskCache?.remove(resolvedUrl)
                        coil.Coil.imageLoader(requireContext()).memoryCache?.remove(coil.memory.MemoryCache.Key(resolvedUrl))
                    } catch (e: Exception) {
                        // Safe catch
                    }
                    ivProfilePhoto?.load(resolvedUrl) {
                        crossfade(true)
                        memoryCachePolicy(coil.request.CachePolicy.DISABLED)
                        diskCachePolicy(coil.request.CachePolicy.DISABLED)
                    }
                } else {
                    ivProfilePhoto?.visibility = View.GONE
                    tvAvatarInitials?.visibility = View.VISIBLE
                }

                if (state.error != null) {
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                }

                if (state.isLoggedOut) {
                    viewModel.logout()
                }
            }
        }
    }

    private fun showSignOutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_logout)
            .setTitle("Sign out?")
            .setMessage("You'll need to sign in again to access your families and map.")
            .setPositiveButton("Sign Out") { _, _ ->
                viewModel.logout()
            }
            .setNegativeButton("Cancel", null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                        ?.setTextColor(
                            androidx.core.content.ContextCompat.getColor(
                                requireContext(), R.color.danger_crimson
                            )
                        )
                }
            }
            .show()
    }

    private fun showEditNameBottomSheet() {
        EditNameBottomSheet().show(childFragmentManager, EditNameBottomSheet.TAG)
    }

    private fun showImageSourceOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Upload Profile Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val cameraPerm = android.Manifest.permission.CAMERA
                        if (androidx.core.content.ContextCompat.checkSelfPermission(
                                requireContext(),
                                cameraPerm
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            startCameraIntent()
                        } else {
                            requestCameraPermissionLauncher.launch(cameraPerm)
                        }
                    }
                    1 -> pickImageLauncher.launch("image/*")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createTempImageUri(): Uri {
        val directory = File(requireContext().cacheDir, "camera_images").apply {
            if (!exists()) mkdirs()
        }
        val file = File.createTempFile("camera_image_", ".jpg", directory)
        return FileProvider.getUriForFile(
            requireContext(),
            "com.labpro.nimons360.fileprovider",
            file
        )
    }

    private fun processAndUploadImage(uri: Uri) {
        val context = requireContext()
        lifecycleScope.launch {
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                if (bitmap == null) {
                    Toast.makeText(context, "Failed to decode image as bitmap", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val format = android.graphics.Bitmap.CompressFormat.JPEG
                val mimeType = "image/jpeg"
                val filename = "profile.jpg"

                val byteArrayOutputStream = ByteArrayOutputStream()
                var quality = 90
                bitmap.compress(format, quality, byteArrayOutputStream)

                while (byteArrayOutputStream.size() > 500 * 1024 && quality > 10) {
                    byteArrayOutputStream.reset()
                    quality -= 10
                    bitmap.compress(format, quality, byteArrayOutputStream)
                }

                val bytes = byteArrayOutputStream.toByteArray()
                val mediaType = mimeType.toMediaTypeOrNull()
                val requestFile = bytes.toRequestBody(mediaType)
                val body = MultipartBody.Part.createFormData("photo", filename, requestFile)

                isUploadingPhoto = true
                viewModel.uploadPhoto(body)
            } catch (e: Exception) {
                Toast.makeText(context, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        if (hasChangedPhoto) {
            val result = Bundle().apply {
                putBoolean(KEY_PROFILE_CHANGED, true)
            }
            parentFragmentManager.setFragmentResult(REQUEST_KEY, result)
        }
    }

    companion object {
        const val TAG = "ProfileFragment"
        const val REQUEST_KEY = "profile_request_key"
        const val KEY_PROFILE_CHANGED = "profile_changed"
    }
}
