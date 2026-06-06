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
import com.labpro.nimons360.core.utils.TokenManager
import com.labpro.nimons360.data.model.map.FavoriteLocationEntity
import com.labpro.nimons360.data.model.map.CustomPin
import com.labpro.nimons360.data.model.map.MapMember
import com.labpro.nimons360.data.model.map.MapSocket
import com.labpro.nimons360.data.model.ui_state.MapUiState
import com.labpro.nimons360.data.repository.CustomPinRepository
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
import kotlin.math.abs

class MapFragment : Fragment(), MarkedLocationBottomSheet.Listener {
    private lateinit var mapRoot: View
    private lateinit var mapView: MapView
    private lateinit var bannerCard: MaterialCardView
    private lateinit var grantCard: MaterialCardView
    private lateinit var btnGrant: MaterialButton
    private lateinit var btnShareStory: MaterialButton
    private lateinit var btnRecenter: MaterialButton
    private lateinit var tvBanner: TextView
    private lateinit var tvStatus: TextView
    private lateinit var pbLocate: ProgressBar

    private lateinit var locationTracker: LocationTracker
    private lateinit var orientationTracker: OrientationTracker
    private lateinit var batteryTracker: BatteryTracker
    private lateinit var netTracker: NetTracker

    private var selfMarker: Marker? = null
    private var lastSelfPosition: GeoPoint? = null
    private var lastSelfRotation: Float? = null
    private var lastSelfPinKey: String? = null
    private val memberMap = linkedMapOf<Int, Marker>()
    private val favoriteMarkers = mutableListOf<Marker>()
    private var locationSettingsDialog: AlertDialog? = null
    private var hasMoved = false
    private var trackersOn = false
    private var locationWatcherOn = false
    private var viewActive = false

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
        )
    }

    private val permissionCall = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.any { it }
        viewModel.setPermission(granted)
        if (granted) {
            if (app().tokenManager.isLocationSharingEnabled()) {
                PresenceServiceController.start(requireContext(), readUser().fullName)
            }
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
        viewActive = true
        setupMap()
        setupTrackers()
        observeState()
        btnGrant.setOnClickListener { askPermission() }
        btnShareStory.setOnClickListener { shareMapStory() }
        btnRecenter.setOnClickListener { recenterMap() }
    }

    override fun onStart() {
        super.onStart()
        mapView.onResume()
        val granted = locationTracker.hasPermission()
        viewModel.setPermission(granted)
        viewModel.bind()
        app().analytics.mapOpened()
        startLocationWatcher()
        if (granted) {
            if (app().tokenManager.isLocationSharingEnabled()) {
                PresenceServiceController.start(requireContext(), readUser().fullName)
            }
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
        viewActive = false
        locationSettingsDialog?.dismiss()
        memberMap.values.forEach { mapView.overlays.remove(it) }
        favoriteMarkers.forEach { mapView.overlays.remove(it) }
        selfMarker?.let { mapView.overlays.remove(it) }
        selfMarker = null
        lastSelfPosition = null
        lastSelfRotation = null
        lastSelfPinKey = null
        memberMap.clear()
        favoriteMarkers.clear()
        super.onDestroyView()
    }

    private fun bind(root: View) {
        mapRoot = root.findViewById(R.id.mapRoot)
        mapView = root.findViewById(R.id.mapView)
        bannerCard = root.findViewById(R.id.bannerCard)
        grantCard = root.findViewById(R.id.grantCard)
        btnGrant = root.findViewById(R.id.btnGrant)
        btnShareStory = root.findViewById(R.id.btnShareStory)
        btnRecenter = root.findViewById(R.id.btnRecenter)
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
                    showMarkedLocationEditor(p)
                }
                return true
            }
        }
        mapView.overlays.add(MapEventsOverlay(mapEventsReceiver))
    }

    private fun showMarkedLocationEditor(point: GeoPoint) {
        if (childFragmentManager.findFragmentByTag(MarkedLocationBottomSheet.TAG) != null) return
        MarkedLocationBottomSheet.newLocation(
            latitude = point.latitude,
            longitude = point.longitude,
            currentLatitude = viewModel.uiState.value.self.latitude,
            currentLongitude = viewModel.uiState.value.self.longitude,
        ).show(childFragmentManager, MarkedLocationBottomSheet.TAG)
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
        if (!viewActive) return

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
        if (!viewActive) return

        favoriteMarkers.forEach { mapView.overlays.remove(it) }
        favoriteMarkers.clear()

        favorites.forEach { fav ->
            val markerIcon = ContextCompat
                .getDrawable(requireContext(), org.osmdroid.library.R.drawable.marker_default)
                ?.mutate()
            markerIcon?.setTint(ContextCompat.getColor(requireContext(), R.color.pin_orange))

            val marker = Marker(mapView).apply {
                position = GeoPoint(fav.latitude, fav.longitude)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = markerIcon
            }

            marker.setOnMarkerClickListener { _, _ ->
                if (childFragmentManager.findFragmentByTag(MarkedLocationBottomSheet.TAG) == null) {
                    MarkedLocationBottomSheet.existingLocation(
                        location = fav,
                        currentLatitude = viewModel.uiState.value.self.latitude,
                        currentLongitude = viewModel.uiState.value.self.longitude,
                    ).show(childFragmentManager, MarkedLocationBottomSheet.TAG)
                }
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
                lastSelfPosition = null
                lastSelfRotation = null
                mapView.invalidate()
            }
            return
        }
        val point = GeoPoint(lat, lon)
        val rotation = state.self.rotation
        var invalidated = false

        if (selfMarker == null) {
            selfMarker = Marker(mapView).apply {
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = state.self.fullName
            }
            mapView.overlays.add(selfMarker)
            lastSelfPosition = null
            lastSelfRotation = null
            invalidated = true
        }

        if (lastSelfPosition != point) {
            selfMarker?.position = point
            lastSelfPosition = point
            invalidated = true
        }

        val selectedSkin = app().tokenManager.getPinSkin()
        val customPin = CustomPin.find(selectedSkin)
        val customFile = customPin
            ?.let { pin -> CustomPinRepository(requireContext()).takeIf { it.isDownloaded(pin) }?.file(pin) }
        val pinKey = customFile?.absolutePath ?: "color:${app().tokenManager.getPinStyle()}"
        val rotDiff = if (lastSelfRotation != null) abs(lastSelfRotation!! - rotation) else Float.MAX_VALUE
        val needsRotationUpdate = customFile == null && rotDiff >= 3f
        if (selfMarker?.icon == null || lastSelfPinKey != pinKey || needsRotationUpdate) {
            selfMarker?.icon = customFile?.let {
                MapPinMaker.custom(requireContext(), it, state.self.fullName)
            }
                ?: MapPinMaker.self(
                    requireContext(),
                    state.self.fullName.firstOrNull()?.uppercase() ?: "Y",
                    state.self.fullName,
                    state.self.rotation,
                    getSelfPinColor(),
                )
            selfMarker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            lastSelfRotation = rotation
            lastSelfPinKey = pinKey
            invalidated = true
        }
        selfMarker?.setInfoWindow(null)

        if (!hasMoved) {
            mapView.controller.animateTo(point)
            hasMoved = true
        }

        if (invalidated) {
            mapView.invalidate()
        }
    }

    private fun renderMembers(state: MapUiState) {
        val keep = state.members.map { it.userId }.toSet()
        var invalidated = false

        memberMap.keys.toList()
            .filterNot(keep::contains)
            .forEach { key ->
                memberMap.remove(key)?.let { marker ->
                    mapView.overlays.remove(marker)
                    invalidated = true
                }
            }

        state.members.forEachIndexed { index, member ->
            var isNewMarker = false
            val marker = memberMap[member.userId] ?: Marker(mapView).also {
                it.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                memberMap[member.userId] = it
                mapView.overlays.add(it)
                isNewMarker = true
                invalidated = true
            }

            val newPosition = GeoPoint(member.latitude, member.longitude)
            if (marker.position != newPosition) {
                marker.position = newPosition
                invalidated = true
            }
            if (marker.title != member.fullName) {
                marker.title = member.fullName
                invalidated = true
            }
            if (marker.snippet != member.email) {
                marker.snippet = member.email
                invalidated = true
            }

            if (isNewMarker || marker.icon == null) {
                marker.icon = MapPinMaker.member(
                    requireContext(),
                    member.fullName.firstOrNull()?.uppercase() ?: "?",
                    member.fullName,
                    pinColors[index % pinColors.size],
                )
                invalidated = true
            }

            marker.setOnMarkerClickListener { _, _ ->
                viewModel.showMember(member)
                true
            }
        }

        if (invalidated) {
            mapView.invalidate()
        }
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

    private fun recenterMap() {
        val state = viewModel.uiState.value.self
        val lat = state.latitude
        val lon = state.longitude
        if (lat == null || lon == null) {
            Toast.makeText(requireContext(), R.string.marked_location_current_unavailable, Toast.LENGTH_SHORT).show()
            return
        }
        mapView.controller.animateTo(GeoPoint(lat, lon))
    }

    override fun onSaveMarkedLocation(location: FavoriteLocationEntity) {
        if (location.id == 0) {
            viewModel.addFavoriteLocation(
                latitude = location.latitude,
                longitude = location.longitude,
                title = location.title,
                description = location.description,
                photoPaths = location.photoPaths,
            )
            app().analytics.favoriteAdded()
        } else {
            viewModel.updateFavoriteLocation(location)
        }
    }

    override fun onDeleteMarkedLocation(location: FavoriteLocationEntity) {
        viewModel.removeFavoriteLocation(location.id)
        app().analytics.favoriteRemoved()
    }

    private fun showInfo(member: MapMember) {
        if (!viewActive || childFragmentManager.isStateSaved) return
        if (childFragmentManager.findFragmentByTag(MemberDetailBottomSheet.TAG) != null) return

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
        app().analytics.memberPopupOpened()
    }

    private fun buildStatus(state: MapUiState): String = when {
        state.showGrant -> getString(R.string.map_permission_waiting)
        state.isLocating -> getString(R.string.map_waiting_location)
        state.socket is MapSocket.Connecting -> getString(R.string.map_connecting)
        state.socket is MapSocket.Connected && state.members.isEmpty() -> getString(R.string.map_live_ready_empty)
        state.socket is MapSocket.Connected -> getString(R.string.map_live_ready_members, state.members.size)
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

    private fun getSelfPinColor(): Int {
        val color = when (app().tokenManager.getPinStyle()) {
            TokenManager.PIN_CORAL -> R.color.secondary_coral
            TokenManager.PIN_BLUE -> R.color.pin_blue
            TokenManager.PIN_PURPLE -> R.color.pin_purple
            TokenManager.PIN_ORANGE -> R.color.pin_orange
            else -> R.color.primary_teal
        }
        return ContextCompat.getColor(requireContext(), color)
    }

    private fun app(): MainApplication {
        return requireActivity().application as MainApplication
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
