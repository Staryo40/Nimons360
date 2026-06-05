package com.labpro.nimons360.data.model.notification

import com.google.gson.annotations.SerializedName

/** Response body for POST /api/notifications/subscribe */
data class SubscribeTokenResponse(
    @SerializedName("data")
    val data: SubscribedData
)

data class SubscribedData(
    @SerializedName("subscribed")
    val subscribed: Boolean
)

/** Response body for POST /api/notifications/unsubscribe */
data class UnsubscribeTokenResponse(
    @SerializedName("data")
    val data: UnsubscribedData
)

data class UnsubscribedData(
    @SerializedName("unsubscribed")
    val unsubscribed: Boolean
)

/** Response body for POST /api/notifications/send */
data class BroadcastNotificationResponse(
    @SerializedName("data")
    val data: SentData
)

data class SentData(
    @SerializedName("sent")
    val sent: Boolean
)

/** Response body for POST /api/notifications/greeting */
data class SendGreetingResponse(
    @SerializedName("data")
    val data: SentData
)

