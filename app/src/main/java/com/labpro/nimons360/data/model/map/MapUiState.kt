package com.labpro.nimons360.data.model.map

data class MapUiState(
    val self: MapSelf,
    val members: List<MapMember> = emptyList(),
    val socket: MapSocket = MapSocket.Idle,
    val selected: MapMember? = null,
    val banner: String? = null,
    val showGrant: Boolean = false,
    val isLocating: Boolean = true,
)
