package com.labpro.nimons360.ui.features.map

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration as AndroidConfiguration
import android.location.LocationManager
import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
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
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.labpro.nimons360.BuildConfig
import com.labpro.nimons360.MainApplication
import com.labpro.nimons360.R
import com.labpro.nimons360.core.utils.InstagramStoryShareHelper
import com.labpro.nimons360.core.utils.TokenManager
import com.labpro.nimons360.data.model.NetworkResult
import com.labpro.nimons360.data.model.family.FamilyWithMembers
import com.labpro.nimons360.data.model.map.FavoriteLocationEntity
import com.labpro.nimons360.data.model.map.CustomPin
import com.labpro.nimons360.data.model.map.MapMember
import com.labpro.nimons360.data.model.map.MapSocket
import com.labpro.nimons360.data.model.ui_state.MapUiState
import com.labpro.nimons360.data.repository.CustomPinRepository
import com.labpro.nimons360.viewmodel.MapViewModel
import com.labpro.nimons360.viewmodel.MapViewModelFactory
import kotlinx.coroutines.Dispatchers
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
    private var mapView: MapView? = null
    private lateinit var bannerCard: MaterialCardView
    private lateinit var grantCard: MaterialCardView
    private lateinit var btnGrant: MaterialButton
    private lateinit var btnShareStory: MaterialButton
    private lateinit var btnRecenter: MaterialButton
    private lateinit var tvBanner: TextView
    private lateinit var tvStatus: TextView
    private lateinit var pbLocate: ProgressBar
    private lateinit var familyFilterScroll: HorizontalScrollView
    private lateinit var familyFilterContainer: LinearLayout

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
    private val memberProfileImageMap = mutableMapOf<String, String>()
    private val memberAvatarBitmaps = mutableMapOf<Int, Bitmap>()
    private val loadingMemberAvatars = mutableSetOf<Int>()
    private val markerIconKeys = mutableMapOf<Int, String>()
    private var selfProfileImageUrl: String? = null
    private var selfAvatarBitmap: Bitmap? = null
    private var loadingSelfAvatar = false
    private var locationSettingsDialog: AlertDialog? = null
    private var hasMoved = false
    private var trackersOn = false
    private var locationWatcherOn = false
    private var viewActive = false
    private var pendingMarkCurrentLocation = false
    private var familyFilters: List<MapFamilyFilter> = emptyList()
    private var selectedFamilyId: Int? = null

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
        } else {
            pendingMarkCurrentLocation = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = requireContext().packageName
        parentFragmentManager.setFragmentResultListener(
            REQUEST_MARK_CURRENT_LOCATION,
            this,
        ) { _, _ ->
            requestMarkCurrentLocation()
        }
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
        mapView?.onResume()
        val granted = locationTracker.hasPermission()
        viewModel.setPermission(granted)
        viewModel.bind()
        app().analytics.mapOpened()
        loadFamilyFilters()
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
        mapView?.onPause()
    }

    override fun onDestroyView() {
        viewActive = false
        locationSettingsDialog?.dismiss()
        mapView?.let { map ->
            memberMap.values.forEach { map.overlays.remove(it) }
            favoriteMarkers.forEach { map.overlays.remove(it) }
            selfMarker?.let { map.overlays.remove(it) }
            map.onDetach()
        }
        selfMarker = null
        lastSelfPosition = null
        lastSelfRotation = null
        lastSelfPinKey = null
        memberMap.clear()
        favoriteMarkers.clear()
        memberProfileImageMap.clear()
        memberAvatarBitmaps.clear()
        loadingMemberAvatars.clear()
        markerIconKeys.clear()
        selfProfileImageUrl = null
        selfAvatarBitmap = null
        loadingSelfAvatar = false
        mapView = null
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
        familyFilterScroll = root.findViewById(R.id.familyFilterScroll)
        familyFilterContainer = root.findViewById(R.id.familyFilterContainer)
        renderFamilyFilters()
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
        val map = mapView ?: return
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(16.0)
        map.controller.setCenter(BANDUNG)
        map.zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)

        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean = false
            override fun longPressHelper(p: GeoPoint?): Boolean {
                if (p != null) {
                    showMarkedLocationEditor(p)
                }
                return true
            }
        }
        map.overlays.add(MapEventsOverlay(mapEventsReceiver))
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
        if (activeMapView() == null) return

        grantCard.isVisible = state.showGrant
        bannerCard.isVisible = !state.banner.isNullOrBlank()
        tvBanner.text = state.banner
        pbLocate.isVisible = state.isLocating
        tvStatus.text = buildStatus(state)

        renderSelf(state)
        renderMembers(state)
        openPendingCurrentLocation(state)

        if (state.selected != null) {
            showInfo(state.selected)
            viewModel.hideMember()
        }
    }

    private fun renderFavorites(favorites: List<FavoriteLocationEntity>) {
        val map = activeMapView() ?: return

        favoriteMarkers.forEach { map.overlays.remove(it) }
        favoriteMarkers.clear()

        favorites.forEach { fav ->
            val markerIcon = ContextCompat
                .getDrawable(requireContext(), org.osmdroid.library.R.drawable.marker_default)
                ?.mutate()
            markerIcon?.setTint(ContextCompat.getColor(requireContext(), R.color.pin_orange))

            val marker = Marker(map).apply {
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
            map.overlays.add(marker)
        }

        map.invalidate()
    }

    private fun renderSelf(state: MapUiState) {
        val map = activeMapView() ?: return
        val lat = state.self.latitude
        val lon = state.self.longitude
        if (lat == null || lon == null) {
            selfMarker?.let { marker ->
                map.overlays.remove(marker)
                selfMarker = null
                lastSelfPosition = null
                lastSelfRotation = null
                map.invalidate()
            }
            return
        }
        val point = GeoPoint(lat, lon)
        val rotation = state.self.rotation
        var invalidated = false

        if (selfMarker == null) {
            selfMarker = Marker(map).apply {
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = state.self.fullName
            }
            map.overlays.add(selfMarker)
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
        val needsRotationUpdate = rotDiff >= 3f

        val imageUrl = selfProfileImageUrl ?: readUser().profileImageUrl
        if (imageUrl.isNullOrBlank() || customFile != null) {
            val actualNeedsRotationUpdate = customFile == null && needsRotationUpdate
            if (selfMarker?.icon == null || lastSelfPinKey != pinKey || actualNeedsRotationUpdate) {
                selfMarker?.icon = customFile?.let {
                    MapPinMaker.custom(requireContext(), it, state.self.fullName)
                }
                    ?: MapPinMaker.self(
                        requireContext(),
                        state.self.fullName.firstOrNull()?.uppercase() ?: "Y",
                        state.self.fullName,
                        rotation,
                        getSelfPinColor(),
                    )
                selfMarker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                lastSelfRotation = rotation
                lastSelfPinKey = pinKey
                invalidated = true
            }
        } else {
            val cleanPath = imageUrl.substringBefore("?")
            val resolvedUrl = if (cleanPath.startsWith("/")) {
                "${BuildConfig.BASE_URL}$cleanPath"
            } else {
                cleanPath
            }

            val cachedBitmap = selfAvatarBitmap
            if (cachedBitmap != null) {
                val currentIconKey = "self_photo:$resolvedUrl"
                if (selfMarker?.icon == null || lastSelfPinKey != currentIconKey || needsRotationUpdate) {
                    selfMarker?.icon = MapPinMaker.selfWithBitmap(
                        requireContext(),
                        cachedBitmap,
                        state.self.fullName,
                        rotation,
                        getSelfPinColor(),
                    )
                    selfMarker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    lastSelfRotation = rotation
                    lastSelfPinKey = currentIconKey
                    invalidated = true
                }
            } else {
                val fallbackIconKey = "self_initials:$rotation"
                if (selfMarker?.icon == null || lastSelfPinKey != fallbackIconKey || needsRotationUpdate) {
                    selfMarker?.icon = MapPinMaker.self(
                        requireContext(),
                        state.self.fullName.firstOrNull()?.uppercase() ?: "Y",
                        state.self.fullName,
                        rotation,
                        getSelfPinColor(),
                    )
                    selfMarker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    lastSelfRotation = rotation
                    lastSelfPinKey = fallbackIconKey
                    invalidated = true
                }

                if (!loadingSelfAvatar) {
                    loadingSelfAvatar = true
                    val loader = coil.Coil.imageLoader(requireContext())
                    val request = coil.request.ImageRequest.Builder(requireContext())
                        .data(resolvedUrl)
                        .allowHardware(false)
                        .bitmapConfig(Bitmap.Config.ARGB_8888)
                        .target(
                            onSuccess = { drawable ->
                                val bitmap = (drawable as? BitmapDrawable)?.bitmap
                                if (bitmap != null) {
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        selfAvatarBitmap = bitmap
                                        loadingSelfAvatar = false
                                        if (viewActive) {
                                            renderSelf(viewModel.uiState.value)
                                        }
                                    }
                                }
                            },
                            onError = {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    loadingSelfAvatar = false
                                }
                            }
                        )
                        .build()
                    loader.enqueue(request)
                }
            }
        }
        selfMarker?.setInfoWindow(null)

        if (!hasMoved) {
            map.controller.animateTo(point)
            hasMoved = true
        }

        if (invalidated) {
            map.invalidate()
        }
    }

    private fun renderMembers(state: MapUiState) {
        val map = activeMapView() ?: return
        val visibleMembers = filteredMembers(state.members)
        val keep = visibleMembers.map { it.userId }.toSet()
        var invalidated = false

        memberMap.keys.toList()
            .filterNot(keep::contains)
            .forEach { key ->
                memberMap.remove(key)?.let { marker ->
                    map.overlays.remove(marker)
                    invalidated = true
                }
            }

        visibleMembers.forEachIndexed { index, member ->
            var isNewMarker = false
            val marker = memberMap[member.userId] ?: Marker(map).also {
                it.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                memberMap[member.userId] = it
                map.overlays.add(it)
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

            val emailKey = member.email.lowercase()
            val imageUrl = memberProfileImageMap[emailKey]

            if (imageUrl.isNullOrBlank()) {
                val fallbackIconKey = "initials:${member.fullName}:${resources.configuration.orientation}"
                if (isNewMarker || marker.icon == null || markerIconKeys[member.userId] != fallbackIconKey) {
                    marker.icon = MapPinMaker.member(
                        requireContext(),
                        member.fullName.firstOrNull()?.uppercase() ?: "?",
                        member.fullName,
                        pinColors[index % pinColors.size],
                        showLabel = resources.configuration.orientation != AndroidConfiguration.ORIENTATION_LANDSCAPE,
                    )
                    markerIconKeys[member.userId] = fallbackIconKey
                    invalidated = true
                }
            } else {
                val cleanPath = imageUrl.substringBefore("?")
                val resolvedUrl = if (cleanPath.startsWith("/")) {
                    "${BuildConfig.BASE_URL}$cleanPath"
                } else {
                    cleanPath
                }

                val cachedBitmap = memberAvatarBitmaps[member.userId]
                if (cachedBitmap != null) {
                    val currentIconKey = "photo:$resolvedUrl:${resources.configuration.orientation}"
                    if (isNewMarker || marker.icon == null || markerIconKeys[member.userId] != currentIconKey) {
                        marker.icon = MapPinMaker.memberWithBitmap(
                            requireContext(),
                            cachedBitmap,
                            member.fullName,
                            pinColors[index % pinColors.size],
                            showLabel = resources.configuration.orientation != AndroidConfiguration.ORIENTATION_LANDSCAPE,
                        )
                        markerIconKeys[member.userId] = currentIconKey
                        invalidated = true
                    }
                } else {
                    val fallbackIconKey = "initials:${member.fullName}:${resources.configuration.orientation}"
                    if (isNewMarker || marker.icon == null || markerIconKeys[member.userId] != fallbackIconKey) {
                        marker.icon = MapPinMaker.member(
                            requireContext(),
                            member.fullName.firstOrNull()?.uppercase() ?: "?",
                            member.fullName,
                            pinColors[index % pinColors.size],
                            showLabel = resources.configuration.orientation != AndroidConfiguration.ORIENTATION_LANDSCAPE,
                        )
                        markerIconKeys[member.userId] = fallbackIconKey
                        invalidated = true
                    }

                    if (!loadingMemberAvatars.contains(member.userId)) {
                        loadingMemberAvatars.add(member.userId)

                        val loader = coil.Coil.imageLoader(requireContext())
                        val request = coil.request.ImageRequest.Builder(requireContext())
                            .data(resolvedUrl)
                            .allowHardware(false)
                            .bitmapConfig(Bitmap.Config.ARGB_8888)
                            .target(
                                onSuccess = { drawable ->
                                    val bitmap = (drawable as? BitmapDrawable)?.bitmap
                                    if (bitmap != null) {
                                        lifecycleScope.launch(Dispatchers.Main) {
                                            memberAvatarBitmaps[member.userId] = bitmap
                                            loadingMemberAvatars.remove(member.userId)
                                            if (viewActive) {
                                                renderMembers(viewModel.uiState.value)
                                            }
                                        }
                                    }
                                },
                                onError = {
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        loadingMemberAvatars.remove(member.userId)
                                    }
                                }
                            )
                            .build()
                        loader.enqueue(request)
                    }
                }
            }

            marker.setOnMarkerClickListener { _, _ ->
                viewModel.showMember(member)
                true
            }
        }

        if (invalidated) {
            map.invalidate()
        }
    }

    private fun startTrackers() {
        if (trackersOn) return
        trackersOn = true
        locationTracker.start(
            onPoint = { point ->
                viewModel.setLocation(point)
                lifecycleScope.launch(Dispatchers.IO) {
                    app().analyticsRepository.recordLocation(
                        point.latitude,
                        point.longitude,
                    )
                }
            },
            onError = viewModel::setLocationError,
        )
        orientationTracker.start(viewModel::setRotation)
        batteryTracker.start(viewModel::setBattery)
        netTracker.start(viewModel::setNet)
    }

    private fun loadFamilyFilters() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = com.labpro.nimons360.data.remote.RetrofitClient.apiService.getMe()
                if (response.isSuccessful) {
                    selfProfileImageUrl = response.body()?.data?.profileImageUrl
                    if (viewActive) {
                        renderSelf(viewModel.uiState.value)
                    }
                }
            } catch (_: Exception) {}
        }

        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = app().familyRepository.getMyFamilies()) {
                is NetworkResult.Success -> {
                    result.data.data.forEach { family ->
                        family.members.forEach { member ->
                            if (!member.profileImageUrl.isNullOrBlank()) {
                                memberProfileImageMap[member.email.lowercase()] = member.profileImageUrl
                            }
                        }
                    }
                    familyFilters = result.data.data.map(::toMapFamilyFilter)
                    if (selectedFamilyId != null &&
                        familyFilters.none { it.id == selectedFamilyId }
                    ) {
                        selectedFamilyId = null
                    }
                    if (viewActive) {
                        renderFamilyFilters()
                        render(viewModel.uiState.value)
                    }
                }
                is NetworkResult.Error -> {
                    familyFilters = emptyList()
                    selectedFamilyId = null
                    if (viewActive) {
                        renderFamilyFilters()
                        render(viewModel.uiState.value)
                    }
                }
            }
        }
    }

    private fun renderFamilyFilters() {
        if (!::familyFilterContainer.isInitialized) return
        val scrollX = familyFilterScroll.scrollX
        familyFilterContainer.removeAllViews()
        familyFilterContainer.addView(
            createFamilyChip(
                id = null,
                label = getString(R.string.all_families_filter),
                accessibilityLabel = getString(R.string.all_families_filter),
            )
        )
        familyFilters.forEach { family ->
            familyFilterContainer.addView(
                createFamilyChip(
                    id = family.id,
                    label = abbreviateFamilyName(family.name),
                    accessibilityLabel = family.name,
                )
            )
        }
        familyFilterScroll.post { familyFilterScroll.scrollTo(scrollX, 0) }
    }

    private fun createFamilyChip(
        id: Int?,
        label: String,
        accessibilityLabel: String,
    ): Chip {
        return Chip(requireContext()).apply {
            text = label
            isCheckable = true
            isChecked = selectedFamilyId == id
            isCheckedIconVisible = false
            isCloseIconVisible = false
            minHeight = resources.getDimensionPixelSize(R.dimen.touch_target_min)
            contentDescription = getString(
                R.string.map_family_filter_description,
                accessibilityLabel,
            )
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ).apply {
                marginEnd = resources.getDimensionPixelSize(R.dimen.spacing_sm)
            }
            setOnClickListener {
                selectedFamilyId = id
                renderFamilyFilters()
                render(viewModel.uiState.value)
            }
        }
    }

    private fun filteredMembers(members: List<MapMember>): List<MapMember> {
        val selected = selectedFamilyId ?: return members
        val emails = familyFilters.firstOrNull { it.id == selected }?.memberEmails ?: return members
        return members.filter { it.email.lowercase() in emails }
    }

    private fun toMapFamilyFilter(family: FamilyWithMembers): MapFamilyFilter {
        return MapFamilyFilter(
            id = family.id,
            name = family.name,
            memberEmails = family.members.mapTo(mutableSetOf()) { it.email.lowercase() },
        )
    }

    private fun abbreviateFamilyName(name: String): String {
        val clean = name.trim()
        return if (clean.length <= MAX_FAMILY_FILTER_NAME) {
            clean
        } else {
            clean.take(MAX_FAMILY_FILTER_NAME - 2).trimEnd() + ".."
        }
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

    private fun requestMarkCurrentLocation() {
        pendingMarkCurrentLocation = true
        if (!viewActive) return

        when {
            !locationTracker.hasPermission() -> askPermission()
            !locationTracker.isGpsReady() -> handleLocationAvailability(showDialog = true)
            else -> openPendingCurrentLocation(viewModel.uiState.value)
        }
    }

    private fun openPendingCurrentLocation(state: MapUiState) {
        if (!pendingMarkCurrentLocation || childFragmentManager.isStateSaved) return
        val latitude = state.self.latitude ?: return
        val longitude = state.self.longitude ?: return

        pendingMarkCurrentLocation = false
        showMarkedLocationEditor(GeoPoint(latitude, longitude))
    }

    private fun recenterMap() {
        val map = activeMapView() ?: return
        val state = viewModel.uiState.value.self
        val lat = state.latitude
        val lon = state.longitude
        if (lat == null || lon == null) {
            Toast.makeText(requireContext(), R.string.marked_location_current_unavailable, Toast.LENGTH_SHORT).show()
            return
        }
        map.controller.animateTo(GeoPoint(lat, lon))
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

    private fun buildStatus(state: MapUiState): String {
        val memberCount = filteredMembers(state.members).size
        return when {
        state.showGrant -> getString(R.string.map_permission_waiting)
        state.isLocating -> getString(R.string.map_waiting_location)
        state.socket is MapSocket.Connecting -> getString(R.string.map_connecting)
        state.socket is MapSocket.Connected && memberCount == 0 -> getString(R.string.map_live_ready_empty)
        state.socket is MapSocket.Connected -> getString(R.string.map_live_ready_members, memberCount)
        else -> getString(R.string.map_idle_status, memberCount)
        }
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

    private fun activeMapView(): MapView? {
        if (!viewActive || view == null) return null
        if (!viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return null
        return mapView
    }

    companion object {
        const val REQUEST_MARK_CURRENT_LOCATION = "request_mark_current_location"
        private const val MAX_FAMILY_FILTER_NAME = 14
        private const val ARG_ID = "arg_id"
        private const val ARG_NAME = "arg_name"
        private const val ARG_EMAIL = "arg_email"
        private const val ARG_PHOTO = "arg_photo"
        private val BANDUNG = GeoPoint(-6.9175, 107.6191)

        fun newInstance(user: com.labpro.nimons360.data.model.user.UserData): MapFragment = MapFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_ID, user.id)
                putString(ARG_NAME, user.fullName)
                putString(ARG_EMAIL, user.email)
                putString(ARG_PHOTO, user.profileImageUrl)
            }
        }
    }

    private fun readUser() = com.labpro.nimons360.data.model.user.UserData(
        id = requireArguments().getInt(ARG_ID),
        nim = "",
        email = requireArguments().getString(ARG_EMAIL).orEmpty(),
        fullName = requireArguments().getString(ARG_NAME).orEmpty(),
        profileImageUrl = requireArguments().getString(ARG_PHOTO),
    )

    private data class MapFamilyFilter(
        val id: Int,
        val name: String,
        val memberEmails: Set<String>,
    )
}
