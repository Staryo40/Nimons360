package com.labpro.nimons360.ui.features.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PresenceServiceController {
    fun start(context: Context, fullName: String) {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) return

        val intent = Intent(context, PresenceForegroundService::class.java)
            .putExtra(PresenceForegroundService.EXTRA_FULL_NAME, fullName)
        ContextCompat.startForegroundService(context, intent)
    }

    fun stop(context: Context) {
        val serviceIntent = Intent(context, PresenceForegroundService::class.java)
        context.stopService(serviceIntent)
    }
}
