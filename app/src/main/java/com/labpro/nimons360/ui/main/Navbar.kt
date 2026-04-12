package com.labpro.nimons360.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.labpro.nimons360.data.enums.MainScreenEnum

@Composable
fun Navbar(
    currentScreen: MainScreenEnum,
    onScreenSelected: (MainScreenEnum) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == MainScreenEnum.HOME,
            onClick = { onScreenSelected(MainScreenEnum.HOME) },
            label = { Text("Home") },
            icon = {
                Icon(Icons.Default.Home, contentDescription = "Home")
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        NavigationBarItem(
            selected = currentScreen == MainScreenEnum.MAP,
            onClick = { onScreenSelected(MainScreenEnum.MAP) },
            label = { Text("Map") },
            icon = {
                Icon(Icons.Default.LocationOn, contentDescription = "Map")
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationBarItem(
            selected = currentScreen == MainScreenEnum.FAMILY,
            onClick = { onScreenSelected(MainScreenEnum.FAMILY) },
            label = { Text("Family") },
            icon = {
                Icon(Icons.Default.Group, contentDescription = "Family")
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}