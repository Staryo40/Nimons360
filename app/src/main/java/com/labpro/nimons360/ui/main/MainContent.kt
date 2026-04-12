package com.labpro.nimons360.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.labpro.nimons360.data.enums.MainScreenEnum
import com.labpro.nimons360.data.model.UserData
import com.labpro.nimons360.ui.main.screens.FamilyCompose
import com.labpro.nimons360.ui.main.screens.HomeCompose
import com.labpro.nimons360.ui.main.screens.MapCompose

@Composable
fun MainContent(user: UserData) {
    var currentScreen by remember { mutableStateOf(MainScreenEnum.HOME) }

    Scaffold(
        bottomBar = {
            Navbar(
                currentScreen = currentScreen,
                onScreenSelected = { currentScreen = it }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentScreen) {
                MainScreenEnum.HOME -> HomeCompose(user)
                MainScreenEnum.FAMILY -> FamilyCompose(user)
                MainScreenEnum.MAP -> MapCompose(user)
            }
        }
    }
}