package com.labpro.nimons360.core.navigation

import android.content.Intent
import android.net.Uri

data class FamilyDeepLink(
    val familyId: Int,
    val code: String?,
) {
    fun toUriString(): String {
        return Uri.Builder()
            .scheme(SCHEME)
            .authority(HOST)
            .appendPath(familyId.toString())
            .apply {
                if (!code.isNullOrBlank()) {
                    appendQueryParameter(QUERY_CODE, code)
                }
            }
            .build()
            .toString()
    }

    companion object {
        const val EXTRA_URI = "com.labpro.nimons360.extra.FAMILY_DEEP_LINK_URI"

        private const val SCHEME = "nimons360"
        private const val HOST = "family"
        private const val QUERY_CODE = "code"

        fun fromIntent(intent: Intent?): FamilyDeepLink? {
            val directLink = fromUri(intent?.data)
            if (directLink != null) return directLink

            val extraUri = intent
                ?.getStringExtra(EXTRA_URI)
                ?.let(Uri::parse)

            return fromUri(extraUri)
        }

        fun fromUri(uri: Uri?): FamilyDeepLink? {
            if (uri?.scheme != SCHEME || uri.host != HOST) return null

            val familyId = uri.pathSegments.firstOrNull()?.toIntOrNull() ?: return null
            return FamilyDeepLink(
                familyId = familyId,
                code = uri.getQueryParameter(QUERY_CODE),
            )
        }
    }
}
