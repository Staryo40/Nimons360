package com.labpro.nimons360.core.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Manages the JWT authentication token using [EncryptedSharedPreferences].
 *
 * All data is encrypted with AES-256-GCM (values) and AES-256-SIV (keys),
 * backed by the Android Keystore — (OWASP M2: Insecure Data Storage).
 *
 * Inject class via [MainApplication];
 */
class TokenManager(context: Context) {

    // Runs when constructed
    // - EncryptedSharedPreferences.create has if/else for if encrypted preferences already exists or not
    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    /** Persist the JWT returned after a successful login. */
    fun saveToken(token: String, expiresAt: Long) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .apply()
        Log.d(TAG, "Saved token, expiresAt=$expiresAt")
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun isLoggedIn(): Boolean {
        val token = getToken() ?: return false
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)

        Log.d(TAG, "token=$token")
        Log.d(TAG, "expiresAt=$expiresAt")
        Log.d(TAG, "now=${System.currentTimeMillis()}")

        return System.currentTimeMillis() < expiresAt
    }

    fun clearToken() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_EXPIRES_AT)
            .apply()
    }

    fun isLocationSharingEnabled(): Boolean {
        return prefs.getBoolean(KEY_SHARE_LOCATION, true)
    }

    fun setLocationSharingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHARE_LOCATION, enabled).apply()
    }

    fun isNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    companion object {
        private const val TAG = "TokenManager"
        private const val FILE_NAME = "nimons360_secure_prefs"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_EXPIRES_AT = "expires_at"
        private const val KEY_SHARE_LOCATION = "share_location"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    }
}