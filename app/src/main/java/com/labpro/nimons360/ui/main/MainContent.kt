package com.labpro.nimons360.ui.main

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AddLocationAlt
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.labpro.nimons360.MainApplication
import com.labpro.nimons360.R
import com.labpro.nimons360.data.enums.FamilyFilter
import com.labpro.nimons360.data.enums.MainScreenEnum
import com.labpro.nimons360.core.navigation.FamilyDeepLink
import com.labpro.nimons360.data.model.user.UserData
import com.labpro.nimons360.ui.features.families.CreateFamilyFragment
import com.labpro.nimons360.ui.features.families.FamilyDetailFragment
import com.labpro.nimons360.ui.features.map.MapFragment
import com.labpro.nimons360.ui.features.profile.ProfileFragment
import com.labpro.nimons360.ui.main.screens.FamilyCompose
import com.labpro.nimons360.ui.main.screens.HomeCompose
import com.labpro.nimons360.ui.main.screens.MapCompose
import com.labpro.nimons360.ui.main.shared.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    user: UserData,
    currentScreen: MainScreenEnum,
    onScreenChange: (MainScreenEnum) -> Unit,
    pendingFamilyDeepLink: FamilyDeepLink? = null,
    onFamilyDeepLinkHandled: () -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val fm = (context as FragmentActivity).supportFragmentManager
    val app = context.applicationContext as MainApplication
    val layoutDirection = LocalLayoutDirection.current
    val safeDrawingPadding = WindowInsets.safeDrawing.asPaddingValues()
    val symmetricSafeHorizontalPadding = maxOf(
        safeDrawingPadding.calculateStartPadding(layoutDirection),
        safeDrawingPadding.calculateEndPadding(layoutDirection),
    )
    var addMenuExpanded by remember { mutableStateOf(false) }
    var requestedFamilyFilter by remember { mutableStateOf<FamilyFilter?>(null) }

    LaunchedEffect(currentScreen) {
        if (currentScreen == MainScreenEnum.FAMILY) {
            app.analytics.familyOpened()
        }
    }

    fun openProfile() {
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            if (!fm.isStateSaved && fm.findFragmentByTag(ProfileFragment.TAG) == null) {
                app.analytics.profileOpened()
                ProfileFragment().show(fm, ProfileFragment.TAG)
            }
        }
    }

    fun openCreateFamily() {
        addMenuExpanded = false
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            if (!fm.isStateSaved && fm.findFragmentByTag(CreateFamilyFragment.TAG) == null) {
                CreateFamilyFragment().show(fm, CreateFamilyFragment.TAG)
            }
        }
    }

    fun markCurrentLocation() {
        addMenuExpanded = false
        if (fm.isStateSaved) return
        fm.setFragmentResult(MapFragment.REQUEST_MARK_CURRENT_LOCATION, Bundle.EMPTY)
        onScreenChange(MainScreenEnum.MAP)
    }

    fun openFamilyDetail(familyId: Int, prefillCode: String? = null): Boolean {
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            if (!fm.isStateSaved && fm.findFragmentByTag(FamilyDetailFragment.TAG) == null) {
                app.analytics.familyOpened()
                FamilyDetailFragment
                    .newInstance(
                        familyId = familyId,
                        currentUserEmail = user.email,
                        prefillCode = prefillCode,
                    )
                    .show(fm, FamilyDetailFragment.TAG)
                return true
            }
        }
        return false
    }

    LaunchedEffect(pendingFamilyDeepLink) {
        pendingFamilyDeepLink?.let { link ->
            if (openFamilyDetail(link.familyId, link.code)) {
                onFamilyDeepLinkHandled()
            }
        }
    }

    val screenTitle = when (currentScreen) {
        MainScreenEnum.HOME   -> "Nimons360"
        MainScreenEnum.MAP    -> context.getString(R.string.map_title)
        MainScreenEnum.FAMILY -> context.getString(R.string.title_browse_families)
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Top,
                        ),
                    )
                    .padding(top = 6.dp),
            ) {
                TopAppBar(
                    modifier = Modifier.padding(
                        horizontal = symmetricSafeHorizontalPadding,
                    ),
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = {
                        Text(
                            text       = screenTitle,
                            style      = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.semantics {
                                heading()
                                contentDescription = context.getString(
                                    R.string.cd_screen_title,
                                    screenTitle,
                                )
                            },
                        )
                    },
                    actions = {
                        UserAvatar(
                            name = user.fullName,
                            size = 40,
                            profileImageUrl = user.profileImageUrl,
                            contentDescription = stringResource(
                                R.string.cd_profile_avatar,
                                user.fullName,
                            ),
                            modifier = Modifier.padding(end = 16.dp),
                            onClick = { openProfile() }
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor    = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color     = MaterialTheme.colorScheme.outline,
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
                    onScreenSelected = {
                        requestedFamilyFilter = null
                        onScreenChange(it)
                    },
                )
            }
        },
        floatingActionButton = {
            if (currentScreen != MainScreenEnum.MAP) {
                Box {
                    DropdownMenu(
                        expanded = addMenuExpanded,
                        onDismissRequest = { addMenuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_add_family)) },
                            onClick = { openCreateFamily() },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.GroupAdd,
                                    contentDescription = null,
                                )
                            },
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_mark_location)) },
                            onClick = { markCurrentLocation() },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.AddLocationAlt,
                                    contentDescription = null,
                                )
                            },
                        )
                    }

                    FloatingActionButton(
                        onClick = { addMenuExpanded = true },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(64.dp),
                        elevation = FloatingActionButtonDefaults.elevation(6.dp),
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.cd_add_actions),
                            modifier = Modifier.size(28.dp),
                        )
                    }
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
                MainScreenEnum.HOME -> HomeCompose(
                    user          = user,
                    onFamilyClick = { openFamilyDetail(it) },
                    onViewAllFamilies = {
                        requestedFamilyFilter = FamilyFilter.MY_FAMILIES
                        onScreenChange(MainScreenEnum.FAMILY)
                    },
                )
                MainScreenEnum.MAP -> MapCompose(user = user)
                MainScreenEnum.FAMILY -> FamilyCompose(
                    user          = user,
                    onFamilyClick = { openFamilyDetail(it) },
                    initialFilter = requestedFamilyFilter,
                )
            }
        }
    }
}
