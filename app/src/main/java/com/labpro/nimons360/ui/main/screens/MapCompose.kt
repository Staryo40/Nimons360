package com.labpro.nimons360.ui.main.screens

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import com.labpro.nimons360.data.model.user.UserData
import com.labpro.nimons360.ui.features.map.MapFragment

@Composable
fun MapCompose(user: UserData, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val host = context as AppCompatActivity
    val frameId = rememberSaveable { View.generateViewId() }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = {
            FragmentContainerView(it).apply {
                id = frameId
                ensureMapFragment(host, user, this)
            }
        },
        update = {
            ensureMapFragment(host, user, it)
        }
    )

    DisposableEffect(host, frameId) {
        onDispose {
            if (host.isChangingConfigurations) return@onDispose
            val fm = host.supportFragmentManager
            if (!fm.isStateSaved) {
                fm.findFragmentByTag(TAG)?.let { fragment ->
                    fm.commit {
                        setReorderingAllowed(true)
                        remove(fragment)
                    }
                }
            }
        }
    }
}

private const val TAG = "map_fragment"

private fun ensureMapFragment(
    host: AppCompatActivity,
    user: UserData,
    container: FragmentContainerView,
) {
    val fm = host.supportFragmentManager
    if (fm.isStateSaved) return

    val fragment = fm.findFragmentByTag(TAG)
    if (fragment == null) {
        container.post {
            if (!fm.isStateSaved && fm.findFragmentByTag(TAG) == null) {
                fm.commit {
                    setReorderingAllowed(true)
                    add(container.id, MapFragment.newInstance(user), TAG)
                }
            }
        }
        return
    }

    if (fragment.view?.parent !== container) {
        container.post {
            if (!fm.isStateSaved) {
                fm.commit {
                    setReorderingAllowed(true)
                    replace(container.id, MapFragment.newInstance(user), TAG)
                }
            }
        }
    }
}
