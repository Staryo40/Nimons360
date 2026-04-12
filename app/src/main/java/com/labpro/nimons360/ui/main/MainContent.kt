package com.labpro.nimons360.ui.main

import android.R.attr.thickness
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.labpro.nimons360.data.enums.MainScreenEnum
import com.labpro.nimons360.data.model.user.UserData
import com.labpro.nimons360.ui.main.screens.FamilyCompose
import com.labpro.nimons360.ui.main.screens.HomeCompose
import com.labpro.nimons360.ui.main.screens.MapCompose
import com.labpro.nimons360.ui.main.shared.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    user: UserData,
    onProfileClick: () -> Unit = {},
    onCreateFamily: () -> Unit = {},
) {
    var currentScreen by remember { mutableStateOf(MainScreenEnum.HOME) }

    val screenTitle = when (currentScreen) {
        MainScreenEnum.HOME   -> "Nimons360"
        MainScreenEnum.MAP    -> "Map"
        MainScreenEnum.FAMILY -> "Families"
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = screenTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = onProfileClick,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(48.dp)
                        ) {
                            UserAvatar(
                                name = user.fullName,
                                size = 48,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        bottomBar = {
            Column {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color     = MaterialTheme.colorScheme.outline,
                )
                Navbar(
                    currentScreen    = currentScreen,
                    onScreenSelected = { currentScreen = it },
                )
            }
        },
        floatingActionButton = {
            if (currentScreen != MainScreenEnum.MAP) {
                FloatingActionButton(
                    onClick          = onCreateFamily,
                    containerColor   = MaterialTheme.colorScheme.secondary,
                    contentColor     = MaterialTheme.colorScheme.onSecondary,
                    modifier         = Modifier.size(64.dp),
                    elevation        = FloatingActionButtonDefaults.elevation(6.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Family", modifier = Modifier.size(28.dp))
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (currentScreen) {
                MainScreenEnum.HOME   -> HomeCompose(user)
                MainScreenEnum.MAP    -> MapCompose(user)
                MainScreenEnum.FAMILY -> FamilyCompose(user)
            }
        }
    }
}