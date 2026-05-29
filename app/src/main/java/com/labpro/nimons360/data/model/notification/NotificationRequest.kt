package com.labpro.nimons360.data.model.notification

import com.google.gson.annotations.SerializedName

/** Request body for POST /api/notifications/subscribe */
data class SubscribeTokenRequest(
    @SerializedName("fcmToken")
    val fcmToken: String
)

/** Request body for POST /api/notifications/send */
data class BroadcastNotificationRequest(
    @SerializedName("familyId")
    val familyId: Int,
    @SerializedName("message")
    val message: String
)

/** Request body for POST /api/notifications/greeting */
data class SendGreetingRequest(
    @SerializedName("familyId")
    val familyId: Int,
    @SerializedName("targetUserId")
    val targetUserId: Int,
    @SerializedName("message")
    val message: String
)
