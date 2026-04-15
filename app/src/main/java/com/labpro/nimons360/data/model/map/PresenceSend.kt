package com.labpro.nimons360.data.model.map

data class PresenceSend(
    val type: String = TYPE,
    val payload: Payload,
    val timestamp: String,
) {
    data class Payload(
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val rotation: Float,
        val batteryLevel: Int?,
        val isCharging: Boolean?,
        val internetStatus: String,
        val metadata: Map<String, String> = emptyMap(),
    )

    companion object {
        const val TYPE = "update_presence"
    }
}
