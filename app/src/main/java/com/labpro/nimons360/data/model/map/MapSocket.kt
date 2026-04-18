package com.labpro.nimons360.data.model.map

sealed class MapSocket {
    data object Idle : MapSocket()
    data object Connecting : MapSocket()
    data object Connected : MapSocket()
    data class Error(val message: String) : MapSocket()
}
