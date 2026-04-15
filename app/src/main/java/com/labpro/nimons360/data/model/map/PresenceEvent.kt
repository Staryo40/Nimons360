package com.labpro.nimons360.data.model.map

import com.google.gson.annotations.SerializedName

data class PresenceEvent(
    val type: String,
    val payload: Payload?,
    val timestamp: String?,
) {
    data class Payload(
        val userId: Int,
        val email: String,
        @SerializedName("fullName") val fullName: String,
        val latitude: Double,
        val longitude: Double,
        val rotation: Float,
        val batteryLevel: Int?,
        val isCharging: Boolean?,
        val internetStatus: String?,
    )
}
