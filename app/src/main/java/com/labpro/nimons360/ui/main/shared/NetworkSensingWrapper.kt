package com.labpro.nimons360.ui.main.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.labpro.nimons360.core.network.NetworkMonitor

@Composable
fun NetworkSensingWrapper(
    networkMonitor: NetworkMonitor,
    content: @Composable () -> Unit
) {
    val isConnected by networkMonitor.isConnected.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        if (!isConnected) {
            DisconnectedDialog()
        }
    }
}