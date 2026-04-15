package com.labpro.nimons360.data.model.map

data class MapSelf(
    val userId: Int,
    val fullName: String,
    val email: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val rotation: Float = 0f,
    val batteryLevel: Int? = null,
    val isCharging: Boolean? = null,
    val internetStatus: String? = null,
)
