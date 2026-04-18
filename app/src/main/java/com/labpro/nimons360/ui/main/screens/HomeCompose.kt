package com.labpro.nimons360.ui.main.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.labpro.nimons360.MainApplication
import com.labpro.nimons360.ui.features.families.CreateFamilyFragment
import com.labpro.nimons360.ui.features.families.FamilyDetailFragment
import com.labpro.nimons360.ui.main.shared.EmptyStateCard
import com.labpro.nimons360.ui.main.shared.LoadingSection
import com.labpro.nimons360.ui.main.shared.RowDivider
import com.labpro.nimons360.ui.main.shared.SectionHeader
import com.labpro.nimons360.viewmodel.HomeViewModel
import com.labpro.nimons360.viewmodel.HomeViewModelFactory
import com.labpro.nimons360.data.model.user.UserData
import com.labpro.nimons360.ui.main.screens.home.DiscoverFamilyItem
import com.labpro.nimons360.ui.main.screens.home.MyFamilyCard
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeCompose(
    user: UserData,
    onFamilyClick: (familyId: Int) -> Unit = {}
) {
    val context = LocalContext.current
    val vm: HomeViewModel = viewModel(
        factory = remember {
            val app = context.applicationContext as MainApplication
            HomeViewModelFactory(app.familyRepository)
        }
    )

    val state by vm.uiState.collectAsState()
    val isRefreshing = state.isLoadingMyFamilies || state.isLoadingDiscover

    val fm = remember { (context as FragmentActivity).supportFragmentManager }

    DisposableEffect(fm) {
        val callback = object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentDestroyed(fragmentManager: FragmentManager, fragment: Fragment) {
                if (fragment is CreateFamilyFragment || fragment is FamilyDetailFragment) {
                    vm.refresh()
                }
            }
        }

        fm.registerFragmentLifecycleCallbacks(callback, false)

        onDispose {
            fm.unregisterFragmentLifecycleCallbacks(callback)
        }
    }

    LaunchedEffect(Unit) {
//        if (state.myFamilies.isEmpty() && state.discoverFamilies.isEmpty()) {
            vm.refresh()
//        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { vm.refresh() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item { SectionHeader("MY FAMILIES") }

            item {
                when {
                    state.isLoadingMyFamilies -> LoadingSection(Modifier.height(148.dp))
                    state.myFamilies.isEmpty() -> EmptyStateCard(
                        "You haven't joined any families yet.",
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    else -> LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 8.dp),
                    ) {
                        items(state.myFamilies) { family ->
                            MyFamilyCard(
                                family = family,
                                onClick = { onFamilyClick(family.id) }
                            )
                        }
                    }
                }
            }

            item { SectionHeader("DISCOVER FAMILIES") }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    ),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        when {
                            state.isLoadingDiscover -> {
                                LoadingSection()
                            }

                            state.discoverFamilies.isEmpty() -> {
                                Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text("No new families to discover right now.", style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            else -> {
                                state.discoverFamilies.forEachIndexed { index, family ->
                                    DiscoverFamilyItem(
                                        family = family,
                                        onJoin = { onFamilyClick(family.id) },
                                    )

                                    if (index < state.discoverFamilies.lastIndex) {
                                        RowDivider()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}