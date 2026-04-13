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
import com.labpro.nimons360.MainApplication
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
    vm: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            (LocalContext.current.applicationContext as MainApplication).familyRepository
        )
    ),
) {
    val state by vm.uiState.collectAsState()
    val isRefreshing = state.isLoadingMyFamilies || state.isLoadingDiscover

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
                            MyFamilyCard(family = family, onClick = { })
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
                                        onJoin = { },
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