package com.labpro.nimons360.core.analytics

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class AppAnalytics(context: Context) {
    private val firebase = FirebaseAnalytics.getInstance(context.applicationContext)
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE,
    )

    fun mapOpened() {
        log(EVENT_MAP_OPENED)
        increment(KEY_MAP_OPENED)
    }

    fun profileOpened() = log(EVENT_PROFILE_OPENED)

    fun familyOpened() = log(EVENT_FAMILY_OPENED)

    fun liveOpened(isHost: Boolean) = log(
        EVENT_LIVE_OPENED,
        PARAM_ROLE to if (isHost) ROLE_HOST else ROLE_VIEWER,
    )

    fun locationShared(enabled: Boolean) = log(
        EVENT_LOCATION_SHARED,
        PARAM_ENABLED to enabled,
    )

    fun favoriteAdded() {
        log(EVENT_FAVORITE_ADDED)
        increment(KEY_FAVORITE_ADDED)
    }

    fun favoriteRemoved() {
        log(EVENT_FAVORITE_REMOVED)
        increment(KEY_FAVORITE_REMOVED)
    }

    fun pinCustomized(style: String) {
        log(
            EVENT_PIN_CUSTOMIZED,
            PARAM_STYLE to style,
        )
        increment(KEY_PIN_CUSTOMIZED)
    }

    fun memberPopupOpened() {
        log(EVENT_MEMBER_POPUP_OPENED)
        increment(KEY_MEMBER_POPUP_OPENED)
    }

    fun analyticsOpened() = log(EVENT_ANALYTICS_OPENED)

    fun summary(): AnalyticsSummary {
        return AnalyticsSummary(
            mapOpened = prefs.getInt(KEY_MAP_OPENED, 0),
            favoriteAdded = prefs.getInt(KEY_FAVORITE_ADDED, 0),
            favoriteRemoved = prefs.getInt(KEY_FAVORITE_REMOVED, 0),
            pinCustomized = prefs.getInt(KEY_PIN_CUSTOMIZED, 0),
            memberPopupOpened = prefs.getInt(KEY_MEMBER_POPUP_OPENED, 0),
        )
    }

    fun resetSummary() {
        prefs.edit().clear().apply()
    }

    private fun log(event: String, vararg params: Pair<String, Any>) {
        val bundle = Bundle()
        params.forEach { (key, value) ->
            when (value) {
                is Boolean -> bundle.putBoolean(key, value)
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Float -> bundle.putFloat(key, value)
                is String -> bundle.putString(key, value)
            }
        }
        runCatching { firebase.logEvent(event, bundle) }
    }

    private fun increment(key: String) {
        val next = prefs.getInt(key, 0) + 1
        prefs.edit().putInt(key, next).apply()
    }

    companion object {
        private const val PREF_NAME = "nimons360_analytics_summary"

        private const val EVENT_MAP_OPENED = "map_opened"
        private const val EVENT_PROFILE_OPENED = "profile_opened"
        private const val EVENT_FAMILY_OPENED = "family_opened"
        private const val EVENT_LIVE_OPENED = "live_opened"
        private const val EVENT_LOCATION_SHARED = "location_shared"
        private const val EVENT_FAVORITE_ADDED = "favorite_added"
        private const val EVENT_FAVORITE_REMOVED = "favorite_removed"
        private const val EVENT_PIN_CUSTOMIZED = "pin_customized"
        private const val EVENT_MEMBER_POPUP_OPENED = "member_popup_opened"
        private const val EVENT_ANALYTICS_OPENED = "analytics_opened"

        private const val PARAM_ENABLED = "enabled"
        private const val PARAM_STYLE = "style"
        private const val PARAM_ROLE = "role"
        private const val ROLE_HOST = "host"
        private const val ROLE_VIEWER = "viewer"

        private const val KEY_MAP_OPENED = "map_opened_count"
        private const val KEY_FAVORITE_ADDED = "favorite_added_count"
        private const val KEY_FAVORITE_REMOVED = "favorite_removed_count"
        private const val KEY_PIN_CUSTOMIZED = "pin_customized_count"
        private const val KEY_MEMBER_POPUP_OPENED = "member_popup_opened_count"
    }
}

data class AnalyticsSummary(
    val mapOpened: Int,
    val favoriteAdded: Int,
    val favoriteRemoved: Int,
    val pinCustomized: Int,
    val memberPopupOpened: Int,
) {
    val isEmpty: Boolean
        get() = mapOpened == 0 &&
            favoriteAdded == 0 &&
            favoriteRemoved == 0 &&
            pinCustomized == 0 &&
            memberPopupOpened == 0
}
