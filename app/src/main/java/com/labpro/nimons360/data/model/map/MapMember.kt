package com.labpro.nimons360.data.model.map

data class MapMember(
    val userId: Int,
    val fullName: String,
    val email: String,
    val latitude: Double,
    val longitude: Double,
    val rotation: Float,
    val batteryLevel: Int?,
    val isCharging: Boolean?,
    val internetStatus: String?,
    val lastSeen: Long,
)
