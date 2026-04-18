package com.labpro.nimons360.ui.features.map

import com.labpro.nimons360.data.enums.InternetStatus

object MapNetMapper {
    fun toPayload(status: InternetStatus): String? = when (status) {
        InternetStatus.WIFI -> "wifi"
        InternetStatus.DATA -> "mobile"
        InternetStatus.NO_INTERNET -> null
    }
}
