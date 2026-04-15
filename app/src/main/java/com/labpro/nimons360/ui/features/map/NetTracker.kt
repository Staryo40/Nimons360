package com.labpro.nimons360.ui.features.map

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.labpro.nimons360.data.enums.InternetStatus

class NetTracker(context: Context) {
    private val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var call: ((InternetStatus) -> Unit)? = null

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            emit()
        }

        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            emit()
        }

        override fun onLost(network: Network) {
            emit()
        }
    }

    fun start(onChange: (InternetStatus) -> Unit) {
        call = onChange
        emit()
        manager.registerDefaultNetworkCallback(callback)
    }

    fun stop() {
        runCatching { manager.unregisterNetworkCallback(callback) }
        call = null
    }

    private fun emit() {
        call?.invoke(read())
    }

    private fun read(): InternetStatus {
        val network = manager.activeNetwork ?: return InternetStatus.NO_INTERNET
        val caps = manager.getNetworkCapabilities(network) ?: return InternetStatus.NO_INTERNET
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> InternetStatus.WIFI
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> InternetStatus.DATA
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> InternetStatus.WIFI
            else -> InternetStatus.NO_INTERNET
        }
    }
}
