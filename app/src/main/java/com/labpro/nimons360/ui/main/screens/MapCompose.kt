package com.labpro.nimons360.ui.main.screens

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import com.labpro.nimons360.data.model.user.UserData
import com.labpro.nimons360.ui.features.map.MapFragment

@Composable
fun MapCompose(user: UserData, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val host = context as AppCompatActivity
    val frameId = remember { View.generateViewId() }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = {
            FragmentContainerView(it).apply {
                id = frameId
                post {
                    if (host.supportFragmentManager.findFragmentByTag(TAG) == null &&
                        !host.supportFragmentManager.isStateSaved
                    ) {
                        host.supportFragmentManager.commit {
                            setReorderingAllowed(true)
                            replace(frameId, MapFragment.newInstance(user), TAG)
                        }
                    }
                }
            }
        },
        update = {
            it.post {
                val fragment = host.supportFragmentManager.findFragmentByTag(TAG)
                if (fragment == null && !host.supportFragmentManager.isStateSaved) {
                    host.supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace(frameId, MapFragment.newInstance(user), TAG)
                    }
                }
            }
        }
    )

    DisposableEffect(host, frameId) {
        onDispose {
            host.supportFragmentManager.findFragmentByTag(TAG)?.let { fragment ->
                if (!host.isFinishing && !host.isDestroyed && !host.supportFragmentManager.isStateSaved) {
                    host.supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        remove(fragment)
                    }
                }
            }
        }
    }
}

private const val TAG = "map_fragment"
