package com.labpro.nimons360.ui.features.map

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.labpro.nimons360.BuildConfig
import com.labpro.nimons360.MainApplication
import com.labpro.nimons360.R
import com.labpro.nimons360.core.utils.InstagramStoryShareHelper
import com.labpro.nimons360.data.model.map.FavoriteLocationEntity
import com.labpro.nimons360.data.model.map.MapMember
import com.labpro.nimons360.data.model.map.MapSocket
import com.labpro.nimons360.data.model.ui_state.MapUiState
import com.labpro.nimons360.viewmodel.MapViewModel
import com.labpro.nimons360.viewmodel.MapViewModelFactory
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.util.Locale
import kotlin.math.abs

class MapFragment : Fragment() {
    private lateinit var mapRoot: View
    private lateinit var mapView: MapView
    private lateinit var bannerCard: MaterialCardView
    private lateinit var grantCard: MaterialCardView
    private lateinit var btnGrant: MaterialButton
    private lateinit var btnShareStory: MaterialButton
    private lateinit var tvBanner: TextView
    private lateinit var tvStatus: TextView
    private lateinit var pbLocate: ProgressBar

    private lateinit var locationTracker: LocationTracker
    private lateinit var orientationTracker: OrientationTracker
    private lateinit var batteryTracker: BatteryTracker
    private lateinit var netTracker: NetTracker

    private var selfMarker: Marker? = null
    private val memberMap = linkedMapOf<Int, Marker>()
    private val favoriteMarkers = mutableListOf<Marker>()
    private var infoDialog: AlertDialog? = null
    private var locationSettingsDialog: AlertDialog? = null
    private var hasMoved = false
    private var trackersOn = false
    private var locationWatcherOn = false

    private val locationProviderReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION ||
                intent?.action == LocationManager.MODE_CHANGED_ACTION
            ) {
                handleLocationAvailability(showDialog = locationTracker.hasPermission())
            }
        }
    }

    private val viewModel: MapViewModel by viewModels {
        val app = requireActivity().application as MainApplication
        MapViewModelFactory(
            user = readUser(),
            token = { app.tokenManager.getToken() },
            locationRepository = app.locationRepository,
            isLocationSharingEnabled = { app.tokenManager.isLocationSharingEnabled() }
        )
    }

    private val permissionCall = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.any { it }
        viewModel.setPermission(granted)
        if (granted) {
            handleLocationAvailability(showDialog = true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = requireContext().packageName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_map, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind(view)
        setupMap()
        setupTrackers()
        observeState()
        btnGrant.setOnClickListener { askPermission() }
        btnShareStory.setOnClickListener { shareMapStory() }
    }

    override fun onStart() {
        super.onStart()
        mapView.onResume()
        val granted = locationTracker.hasPermission()
        viewModel.setPermission(granted)
        viewModel.bind()
        startLocationWatcher()
        if (granted) {
            handleLocationAvailability(showDialog = true)
        }
    }

    override fun onStop() {
        super.onStop()
        locationSettingsDialog?.dismiss()
        locationSettingsDialog = null
        stopLocationWatcher()
        stopTrackers()
        viewModel.unbind()
        mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        infoDialog?.dismiss()
        locationSettingsDialog?.dismiss()
        memberMap.values.forEach { mapView.overlays.remove(it) }
        favoriteMarkers.forEach { mapView.overlays.remove(it) }
        selfMarker?.let { mapView.overlays.remove(it) }
        selfMarker = null
        memberMap.clear()
        favoriteMarkers.clear()
    }

    private fun bind(root: View) {
        mapRoot = root.findViewById(R.id.mapRoot)
        mapView = root.findViewById(R.id.mapView)
        bannerCard = root.findViewById(R.id.bannerCard)
        grantCard = root.findViewById(R.id.grantCard)
        btnGrant = root.findViewById(R.id.btnGrant)
        btnShareStory = root.findViewById(R.id.btnShareStory)
        tvBanner = root.findViewById(R.id.tvBanner)
        tvStatus = root.findViewById(R.id.tvStatus)
        pbLocate = root.findViewById(R.id.pbLocate)
    }

    private fun shareMapStory() {
        if (mapRoot.width <= 0 || mapRoot.height <= 0) {
            Toast.makeText(requireContext(), R.string.instagram_story_share_error, Toast.LENGTH_SHORT).show()
            return
        }

        btnShareStory.isEnabled = false
        btnShareStory.visibility = View.INVISIBLE

        mapRoot.post {
            try {
                val bitmap = InstagramStoryShareHelper.captureView(mapRoot)
                val file = InstagramStoryShareHelper.writeStoryImage(requireContext(), bitmap)
                val opened = InstagramStoryShareHelper.shareToInstagramStory(
                    activity = requireActivity(),
                    imageFile = file,
                    facebookAppId = BuildConfig.FACEBOOK_APP_ID,
                )

                if (!opened) {
                    Toast.makeText(requireContext(), R.string.instagram_not_installed, Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                Toast.makeText(requireContext(), R.string.instagram_story_share_error, Toast.LENGTH_SHORT).show()
            } finally {
                btnShareStory.visibility = View.VISIBLE
                btnShareStory.isEnabled = true
            }
        }
    }

    private fun setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(16.0)
        mapView.controller.setCenter(BANDUNG)
        mapView.zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)

        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean = false
            override fun longPressHelper(p: GeoPoint?): Boolean {
                if (p != null) {
                    showAddFavoriteDialog(p)
                }
                return true
            }
        }
        mapView.overlays.add(MapEventsOverlay(mapEventsReceiver))
    }

    private fun showAddFavoriteDialog(p: GeoPoint) {
        val input = android.widget.EditText(requireContext()).apply {
            hint = "e.g., Home, Basecamp"
            setSingleLine()
        }

        val container = android.widget.FrameLayout(requireContext()).apply {
            val params = android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(64, 16, 64, 0)
            }
            input.layoutParams = params
            addView(input)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Favorite Location")
            .setMessage("Enter a name for this location:")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val title = input.text.toString().takeIf { it.isNotBlank() } ?: "Favorite Location"
                viewModel.toggleFavoriteLocation(p.latitude, p.longitude, title)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupTrackers() {
        val ctx = requireContext()
        locationTracker = LocationTracker(ctx)
        orientationTracker = OrientationTracker(ctx)
        batteryTracker = BatteryTracker(ctx)
        netTracker = NetTracker(ctx)
    }

    private fun startLocationWatcher() {
        if (locationWatcherOn) return
        val filter = IntentFilter().apply {
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
            addAction(LocationManager.MODE_CHANGED_ACTION)
        }
        ContextCompat.registerReceiver(
            requireContext(),
            locationProviderReceiver,
            filter,
            RECEIVER_NOT_EXPORTED,
        )
        locationWatcherOn = true
    }

    private fun stopLocationWatcher() {
        if (!locationWatcherOn) return
        requireContext().unregisterReceiver(locationProviderReceiver)
        locationWatcherOn = false
    }

    private fun handleLocationAvailability(showDialog: Boolean) {
        if (!locationTracker.hasPermission()) {
            locationSettingsDialog?.dismiss()
            locationSettingsDialog = null
            viewModel.setPermission(false)
            stopTrackers()
            return
        }

        if (!locationTracker.isGpsReady()) {
            stopTrackers()
            viewModel.setLocationEnabled(false)
            if (showDialog) {
                showLocationSettingsDialog()
            }
            return
        }

        locationSettingsDialog?.dismiss()
        locationSettingsDialog = null
        viewModel.setLocationEnabled(true)
        startTrackers()
    }

    private fun showLocationSettingsDialog() {
        if (locationSettingsDialog?.isShowing == true) return
        locationSettingsDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.map_location_off_title)
            .setMessage(R.string.map_location_off_body)
            .setCancelable(false)
            .setPositiveButton(R.string.map_open_location_settings) { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton(R.string.btn_close, null)
            .show()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.favoriteLocations.collect(::renderFavorites) }
            }
        }
    }

    private fun render(state: MapUiState) {
        grantCard.isVisible = state.showGrant
        bannerCard.isVisible = !state.banner.isNullOrBlank()
        tvBanner.text = state.banner
        pbLocate.isVisible = state.isLocating
        tvStatus.text = buildStatus(state)

        renderSelf(state)
        renderMembers(state)

        if (state.selected != null) {
            showInfo(state.selected)
            viewModel.hideMember()
        }
    }

    private fun renderFavorites(favorites: List<FavoriteLocationEntity>) {
        favoriteMarkers.forEach { mapView.overlays.remove(it) }
        favoriteMarkers.clear()

        val defaultIcon = ContextCompat.getDrawable(requireContext(), org.osmdroid.library.R.drawable.marker_default)?.mutate()
        defaultIcon?.setTint(ContextCompat.getColor(requireContext(), R.color.pin_orange))

        favorites.forEach { fav ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(fav.latitude, fav.longitude)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = defaultIcon
            }

            marker.setOnMarkerClickListener { _, _ ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(fav.title)
                    .setMessage("Coordinates:\nLat: ${fav.latitude}\nLon: ${fav.longitude}")
                    .setPositiveButton("Remove") { _, _ ->
                        viewModel.toggleFavoriteLocation(fav.latitude, fav.longitude, "")
                    }
                    .setNegativeButton("Close", null)
                    .show()
                true
            }

            favoriteMarkers.add(marker)
            mapView.overlays.add(marker)
        }

        mapView.invalidate()
    }

    private fun renderSelf(state: MapUiState) {
        val lat = state.self.latitude
        val lon = state.self.longitude
        if (lat == null || lon == null) {
            selfMarker?.let { marker ->
                mapView.overlays.remove(marker)
                selfMarker = null
                mapView.invalidate()
            }
            return
        }
        val point = GeoPoint(lat, lon)

        if (selfMarker == null) {
            selfMarker = Marker(mapView).apply {
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                title = state.self.fullName
            }
            mapView.overlays.add(selfMarker)
        }

        selfMarker?.apply {
            position = point
            icon = MapPinMaker.self(
                requireContext(),
                state.self.fullName.firstOrNull()?.uppercase() ?: "Y",
                state.self.rotation,
            )
            setInfoWindow(null)
        }

        if (!hasMoved) {
            mapView.controller.animateTo(point)
            hasMoved = true
        }

        mapView.invalidate()
    }

    private fun renderMembers(state: MapUiState) {
        val keep = state.members.map { it.userId }.toSet()

        memberMap.keys.toList()
            .filterNot(keep::contains)
            .forEach { key ->
                memberMap.remove(key)?.let { marker ->
                    mapView.overlays.remove(marker)
                }
            }

        state.members.forEachIndexed { index, member ->
            val marker = memberMap[member.userId] ?: Marker(mapView).also {
                it.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                it.setOnMarkerClickListener { _, _ ->
                    viewModel.showMember(member)
                    true
                }
                memberMap[member.userId] = it
                mapView.overlays.add(it)
            }

            marker.position = GeoPoint(member.latitude, member.longitude)
            marker.title = member.fullName
            marker.snippet = member.email
            marker.icon = MapPinMaker.member(
                requireContext(),
                member.fullName.firstOrNull()?.uppercase() ?: "?",
                pinColors[index % pinColors.size],
            )
            marker.setOnMarkerClickListener { _, _ ->
                viewModel.showMember(member)
                true
            }
        }

        mapView.invalidate()
    }

    private fun startTrackers() {
        if (trackersOn) return
        trackersOn = true
        locationTracker.start(
            onPoint = viewModel::setLocation,
            onError = viewModel::setLocationError,
        )
        orientationTracker.start(viewModel::setRotation)
        batteryTracker.start(viewModel::setBattery)
        netTracker.start(viewModel::setNet)
    }

    private fun stopTrackers() {
        if (!trackersOn) return
        trackersOn = false
        locationTracker.stop()
        orientationTracker.stop()
        batteryTracker.stop()
        netTracker.stop()
    }

    private fun askPermission() {
        permissionCall.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        )
    }

    private fun showInfo(member: MapMember) {
        MemberDetailBottomSheet.newInstance(
            userId = member.userId,
            name = member.fullName,
            email = member.email,
            lat = member.latitude,
            lon = member.longitude,
            battery = member.batteryLevel,
            charging = member.isCharging,
            net = member.internetStatus
        ).show(childFragmentManager, MemberDetailBottomSheet.TAG)
    }

    private fun buildStatus(state: MapUiState): String = when {
        state.showGrant -> getString(R.string.map_permission_waiting)
        state.isLocating -> getString(R.string.map_waiting_location)
        state.socket is MapSocket.Connected -> getString(R.string.map_live_ready)
        state.socket is MapSocket.Connecting -> getString(R.string.map_connecting)
        else -> getString(R.string.map_idle_status, state.members.size)
    }

    private val pinColors by lazy {
        listOf(
            ContextCompat.getColor(requireContext(), R.color.pin_red),
            ContextCompat.getColor(requireContext(), R.color.pin_green),
            ContextCompat.getColor(requireContext(), R.color.pin_blue),
            ContextCompat.getColor(requireContext(), R.color.pin_orange),
            ContextCompat.getColor(requireContext(), R.color.pin_purple),
        )
    }

    companion object {
        private const val ARG_ID = "arg_id"
        private const val ARG_NAME = "arg_name"
        private const val ARG_EMAIL = "arg_email"
        private val BANDUNG = GeoPoint(-6.9175, 107.6191)

        fun newInstance(user: com.labpro.nimons360.data.model.user.UserData): MapFragment = MapFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_ID, user.id)
                putString(ARG_NAME, user.fullName)
                putString(ARG_EMAIL, user.email)
            }
        }
    }

    private fun readUser() = com.labpro.nimons360.data.model.user.UserData(
        id = requireArguments().getInt(ARG_ID),
        nim = "",
        email = requireArguments().getString(ARG_EMAIL).orEmpty(),
        fullName = requireArguments().getString(ARG_NAME).orEmpty(),
    )
}
