package com.labpro.nimons360.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.labpro.nimons360.R
import com.labpro.nimons360.data.enums.MainScreenEnum

@Composable
fun Navbar(
    currentScreen: MainScreenEnum,
    onScreenSelected: (MainScreenEnum) -> Unit,
) {
    val homeLabel = stringResource(R.string.nav_home)
    val mapLabel = stringResource(R.string.nav_map)
    val familiesLabel = stringResource(R.string.nav_families)

    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor   = MaterialTheme.colorScheme.primary,
        selectedTextColor   = MaterialTheme.colorScheme.primary,
        indicatorColor      = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f),
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        NavigationBarItem(
            selected = currentScreen == MainScreenEnum.HOME,
            onClick  = { onScreenSelected(MainScreenEnum.HOME) },
            label    = { Text(homeLabel) },
            icon     = {
                Icon(   
                    if (currentScreen == MainScreenEnum.HOME) Icons.Filled.Home
                    else Icons.Outlined.Home,
                    contentDescription = null,
                )
            },
            colors = itemColors,
        )

        NavigationBarItem(
            selected = currentScreen == MainScreenEnum.MAP,
            onClick  = { onScreenSelected(MainScreenEnum.MAP) },
            label    = { Text(mapLabel) },
            icon     = {
                Icon(
                    if (currentScreen == MainScreenEnum.MAP) Icons.Filled.LocationOn
                    else Icons.Outlined.LocationOn,
                    contentDescription = null,
                )
            },
            colors = itemColors,
        )

        NavigationBarItem(
            selected = currentScreen == MainScreenEnum.FAMILY,
            onClick  = { onScreenSelected(MainScreenEnum.FAMILY) },
            label    = { Text(familiesLabel) },
            icon     = {
                Icon(
                    if (currentScreen == MainScreenEnum.FAMILY) Icons.Filled.Group
                    else Icons.Outlined.Group,
                    contentDescription = null,
                )
            },
            colors = itemColors,
        )
    }
}
