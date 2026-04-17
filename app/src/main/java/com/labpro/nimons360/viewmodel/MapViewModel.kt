package com.labpro.nimons360.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labpro.nimons360.data.enums.InternetStatus
import com.labpro.nimons360.data.model.map.MapMember
import com.labpro.nimons360.data.model.map.MapPoint
import com.labpro.nimons360.data.model.map.MapSelf
import com.labpro.nimons360.data.model.map.MapSocket
import com.labpro.nimons360.data.model.map.MapUiState
import com.labpro.nimons360.data.model.map.PresenceEvent
import com.labpro.nimons360.data.model.map.PresenceSend
import com.labpro.nimons360.data.model.user.UserData
import com.labpro.nimons360.ui.features.map.MapNetMapper
import com.labpro.nimons360.ui.features.map.PresenceSocket
import com.labpro.nimons360.data.model.map.FavoriteLocationEntity
import com.labpro.nimons360.data.repository.LocationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

class MapViewModel(
    user: UserData,
    token: () -> String?,
    private val locationRepository: LocationRepository
) : ViewModel() {
    private val selfId = user.id

    private val _uiState = MutableStateFlow(
        MapUiState(
            self = MapSelf(
                userId = user.id,
                fullName = user.fullName,
                email = user.email,
            ),
        )
    )
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val socket = PresenceSocket(
        scope = viewModelScope,
        token = token,
        onState = ::applySocket,
        onEvent = ::applyEvent,
    )

    private var sendJob: Job? = null
    private var trimJob: Job? = null

    val favoriteLocations: StateFlow<List<FavoriteLocationEntity>> = locationRepository.observeFavoriteLocations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleFavoriteLocation(latitude: Double, longitude: Double, title: String) {
        viewModelScope.launch {
            val isFavorite = favoriteLocations.value.any { it.latitude == latitude && it.longitude == longitude }
            if (isFavorite) {
                locationRepository.removeFavoriteLocation(latitude, longitude)
            } else {
                locationRepository.addFavoriteLocation(latitude, longitude, title)
            }
        }
    }

    fun bind() {
        socket.resume()
        socket.connect()
        startSend()
        startTrim()
    }

    fun unbind() {
        sendJob?.cancel()
        trimJob?.cancel()
        socket.close()
    }

    fun setPermission(granted: Boolean) {
        _uiState.value = _uiState.value.copy(
            showGrant = !granted,
            isLocating = granted && _uiState.value.self.latitude == null,
        )
    }

    fun setLocation(point: MapPoint) {
        _uiState.value = _uiState.value.copy(
            self = _uiState.value.self.copy(
                latitude = point.latitude,
                longitude = point.longitude,
            ),
            banner = null,
            isLocating = false,
        )
    }

    fun setRotation(rotation: Float) {
        _uiState.value = _uiState.value.copy(
            self = _uiState.value.self.copy(rotation = rotation),
        )
    }

    fun setBattery(level: Int?, isCharging: Boolean?) {
        _uiState.value = _uiState.value.copy(
            self = _uiState.value.self.copy(
                batteryLevel = level,
                isCharging = isCharging,
            ),
        )
    }

    fun setNet(status: InternetStatus) {
        val payload = MapNetMapper.toPayload(status)
        _uiState.value = _uiState.value.copy(
            self = _uiState.value.self.copy(internetStatus = payload),
            banner = if (status == InternetStatus.NO_INTERNET) {
                "No internet connection. Live location updates are paused."
            } else {
                _uiState.value.banner?.takeUnless { it.contains("Live map connection lost") }
            },
        )
        if (status != InternetStatus.NO_INTERNET) {
            socket.connect()
        }
    }

    fun showMember(member: MapMember) {
        _uiState.value = _uiState.value.copy(selected = member)
    }

    fun hideMember() {
        _uiState.value = _uiState.value.copy(selected = null)
    }

    fun clearBanner() {
        _uiState.value = _uiState.value.copy(banner = null)
    }

    fun setLocationError(message: String) {
        _uiState.value = _uiState.value.copy(
            banner = message,
            isLocating = false,
        )
    }

    internal fun applySocket(state: MapSocket) {
        val banner = when (state) {
            is MapSocket.Error -> state.message
            MapSocket.Connected -> null
            else -> _uiState.value.banner?.takeUnless { it.contains("Live map connection lost") }
        }
        _uiState.value = _uiState.value.copy(socket = state, banner = banner)
    }

    internal fun applyEvent(
        event: PresenceEvent,
        now: Long = System.currentTimeMillis(),
    ) {
        val body = event.payload ?: return
        if (body.userId == selfId) return

        val member = MapMember(
            userId = body.userId,
            fullName = body.fullName,
            email = body.email,
            latitude = body.latitude,
            longitude = body.longitude,
            rotation = body.rotation,
            batteryLevel = body.batteryLevel,
            isCharging = body.isCharging,
            internetStatus = body.internetStatus,
            lastSeen = now,
        )

        val next = _uiState.value.members
            .filterNot { it.userId == member.userId }
            .plus(member)
            .sortedBy { it.fullName.lowercase() }

        _uiState.value = _uiState.value.copy(members = next)
    }

    private fun startSend() {
        if (sendJob?.isActive == true) return
        sendJob = viewModelScope.launch {
            while (true) {
                delay(SEND_MS)
                val state = _uiState.value
                val lat = state.self.latitude
                val lon = state.self.longitude
                val net = state.self.internetStatus
                if (lat == null || lon == null || net == null) continue

                socket.sendPresence(
                    PresenceSend(
                        payload = PresenceSend.Payload(
                            name = state.self.fullName,
                            latitude = lat,
                            longitude = lon,
                            rotation = state.self.rotation,
                            batteryLevel = state.self.batteryLevel,
                            isCharging = state.self.isCharging,
                            internetStatus = net,
                        ),
                        timestamp = Instant.now().toString(),
                    )
                )
            }
        }
    }

    private fun startTrim() {
        if (trimJob?.isActive == true) return
        trimJob = viewModelScope.launch {
            while (true) {
                delay(TRIM_MS)
                trimOffline()
            }
        }
    }

    internal fun trimOffline(
        now: Long = System.currentTimeMillis(),
    ) {
        val members = _uiState.value.members.filter { now - it.lastSeen <= TIMEOUT_MS }
        if (members.size != _uiState.value.members.size) {
            _uiState.value = _uiState.value.copy(
                members = members,
                selected = _uiState.value.selected?.takeIf { target ->
                    members.any { it.userId == target.userId }
                },
            )
        }
    }

    companion object {
        private const val SEND_MS = 1_000L
        private const val TRIM_MS = 1_000L
        private const val TIMEOUT_MS = 5_000L
    }
}
