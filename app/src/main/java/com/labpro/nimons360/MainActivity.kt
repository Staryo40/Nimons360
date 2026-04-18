package com.labpro.nimons360

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.labpro.nimons360.core.events.AuthEvent
import com.labpro.nimons360.core.events.AuthEventBus
import com.labpro.nimons360.core.network.NetworkMonitor
import com.labpro.nimons360.ui.features.auth.LoginActivity
import com.labpro.nimons360.ui.main.shared.NetworkSensingWrapper
import com.labpro.nimons360.ui.main.MainContent
import com.labpro.nimons360.ui.theme.Nimons360Theme
import com.labpro.nimons360.viewmodel.MainViewModel
import com.labpro.nimons360.viewmodel.MainViewModelFactory
import kotlinx.coroutines.launch


// Homework, whats the difference?:
// Before: ComponentActivity()
// After : AppCompatActivity()
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as MainApplication).userRepository)
    }

    private lateinit var networkMonitor: NetworkMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        networkMonitor = NetworkMonitor(applicationContext)

        observeAuthEvents()
        enableEdgeToEdge()
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            Nimons360Theme {
                NetworkSensingWrapper(networkMonitor = networkMonitor) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            state.isLoading -> {
                                Text("Loading...", style = MaterialTheme.typography.bodyMedium)
                            }

                        state.user != null -> {
                            val user = state.user!!
                            MainContent(
                                user = user
                            )
                        }

                            state.error != null -> {
                                Text(
                                    text = "Error: ${state.error}",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun observeAuthEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AuthEventBus.events.collect { event ->
                    when (event) {
                        is AuthEvent.SessionExpired,
                        is AuthEvent.LoggedOut -> navigateToLogin()
                    }
                }
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}