package com.labpro.nimons360.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labpro.nimons360.data.enums.InternetStatus
import com.labpro.nimons360.data.model.map.MapMember
import com.labpro.nimons360.data.model.map.MapPoint
import com.labpro.nimons360.data.model.map.MapSelf
import com.labpro.nimons360.data.model.map.MapSocket
import com.labpro.nimons360.data.model.map.PresenceEvent
import com.labpro.nimons360.data.model.ui_state.MapUiState
import com.labpro.nimons360.data.model.user.UserData
import com.labpro.nimons360.ui.features.map.MapNetMapper
import com.labpro.nimons360.ui.features.map.PresenceSocket
import com.labpro.nimons360.data.model.map.FavoriteLocationEntity
import com.labpro.nimons360.data.repository.LocationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel(
    user: UserData,
    token: () -> String?,
    private val locationRepository: LocationRepository,
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

    private var trimJob: kotlinx.coroutines.Job? = null

    val favoriteLocations: StateFlow<List<FavoriteLocationEntity>> = locationRepository.observeFavoriteLocations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addFavoriteLocation(
        latitude: Double,
        longitude: Double,
        title: String,
        description: String = "",
        photoPaths: List<String> = emptyList(),
    ) {
        viewModelScope.launch {
            locationRepository.addFavoriteLocation(
                latitude = latitude,
                longitude = longitude,
                title = title,
                description = description,
                photoPaths = photoPaths,
            )
        }
    }

    fun updateFavoriteLocation(entity: FavoriteLocationEntity) {
        viewModelScope.launch {
            locationRepository.updateFavoriteLocation(entity)
        }
    }

    fun removeFavoriteLocation(id: Int) {
        viewModelScope.launch {
            locationRepository.removeFavoriteLocation(id)
        }
    }

    fun bind() {
        socket.resume()
        socket.connect()
        startTrim()
    }

    fun unbind() {
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

    fun setLocationEnabled(enabled: Boolean) {
        val state = _uiState.value
        _uiState.value = if (enabled) {
            state.copy(
                banner = state.banner?.takeUnless { it.contains("location services", ignoreCase = true) },
                isLocating = state.self.latitude == null,
            )
        } else {
            state.copy(
                self = state.self.copy(
                    latitude = null,
                    longitude = null,
                ),
                banner = "Turn on location services to start live map tracking.",
                isLocating = false,
            )
        }
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
        private const val TRIM_MS = 1_000L
        private const val TIMEOUT_MS = 5_000L
    }
}
