package com.labpro.nimons360.data.model.ui_state

import com.labpro.nimons360.data.model.map.MapMember
import com.labpro.nimons360.data.model.map.MapSelf
import com.labpro.nimons360.data.model.map.MapSocket

data class MapUiState(
    val self: MapSelf,
    val members: List<MapMember> = emptyList(),
    val socket: MapSocket = MapSocket.Idle,
    val selected: MapMember? = null,
    val banner: String? = null,
    val showGrant: Boolean = false,
    val isLocating: Boolean = true,
)
