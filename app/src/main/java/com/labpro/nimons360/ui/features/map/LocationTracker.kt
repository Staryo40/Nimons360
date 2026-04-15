package com.labpro.nimons360.ui.features.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.labpro.nimons360.data.model.map.MapPoint

class LocationTracker(context: Context) {
    private val app = context.applicationContext
    private val client: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(app)
    private val manager = app.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var call: ((MapPoint) -> Unit)? = null
    private var fail: ((String) -> Unit)? = null

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val point = result.lastLocation ?: return
            call?.invoke(MapPoint(point.latitude, point.longitude))
        }
    }

    fun hasPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    fun isGpsReady(): Boolean = manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
        manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    @SuppressLint("MissingPermission")
    fun start(
        onPoint: (MapPoint) -> Unit,
        onError: (String) -> Unit,
    ) {
        call = onPoint
        fail = onError

        if (!hasPermission()) {
            onError("Location permission is required to show your position on the map.")
            return
        }

        if (!isGpsReady()) {
            onError("Turn on location services to start live map tracking.")
            return
        }

        client.lastLocation.addOnSuccessListener { point ->
            if (point != null) {
                onPoint(MapPoint(point.latitude, point.longitude))
            }
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_MS)
            .setMinUpdateIntervalMillis(FAST_MS)
            .build()

        client.requestLocationUpdates(request, callback, app.mainLooper)
    }

    fun stop() {
        client.removeLocationUpdates(callback)
        call = null
        fail = null
    }

    companion object {
        private const val UPDATE_MS = 2_000L
        private const val FAST_MS = 1_000L
    }
}
