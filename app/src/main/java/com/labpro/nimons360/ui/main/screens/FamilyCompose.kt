package com.labpro.nimons360.ui.main.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.labpro.nimons360.MainApplication
import com.labpro.nimons360.R
import com.labpro.nimons360.data.enums.FamilyFilter
import com.labpro.nimons360.data.model.user.UserData
import com.labpro.nimons360.ui.main.screens.family.FamilyListItem
import com.labpro.nimons360.ui.main.shared.EmptyStateCard
import com.labpro.nimons360.ui.main.shared.LoadingSection
import com.labpro.nimons360.ui.main.shared.SectionHeader
import com.labpro.nimons360.viewmodel.FamilyViewModel
import com.labpro.nimons360.viewmodel.FamilyViewModelFactory
import androidx.compose.runtime.getValue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyCompose(
    user: UserData,
    onFamilyClick: (familyId: Int) -> Unit = {},
    vm: FamilyViewModel = viewModel(
        factory = FamilyViewModelFactory(
            (LocalContext.current.applicationContext as MainApplication).familyRepository,
        ),
    ),
) {
    val state by vm.uiState.collectAsState()

    val filteredFamilies = state.allFamilies
        .filter {
            it.name.contains(state.searchQuery, ignoreCase = true)
        }
        .let { list ->
            when (state.filter) {
                FamilyFilter.ALL -> list
                FamilyFilter.MY_FAMILIES -> list.filter { it.id in state.myFamilyIds }
            }
        }

    val pinnedFamilies = filteredFamilies.filter { it.id in state.pinnedIds }
    val unpinnedFamilies = filteredFamilies.filter { it.id !in state.pinnedIds }

    LaunchedEffect(Unit) {
            vm.load()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        OutlinedTextField(
            value         = state.searchQuery,
            onValueChange = { vm.setSearch(it) },
            label         = { Text(stringResource(R.string.search_families_label)) },
            placeholder   = { Text(stringResource(R.string.search_families_hint)) },
            leadingIcon   = {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            singleLine = true,
            modifier   = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape  = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )

        Row(
            modifier            = Modifier.padding(start = 16.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = state.filter == FamilyFilter.ALL,
                onClick  = { vm.setFilter(FamilyFilter.ALL) },
                label    = { Text(stringResource(R.string.all_families)) },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor     = MaterialTheme.colorScheme.onPrimary,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
            FilterChip(
                selected = state.filter == FamilyFilter.MY_FAMILIES,
                onClick  = { vm.setFilter(FamilyFilter.MY_FAMILIES) },
                label    = { Text(stringResource(R.string.my_families_filter)) },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor     = MaterialTheme.colorScheme.onPrimary,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }

        when {
            state.isLoading -> LoadingSection(Modifier.fillMaxSize())
            state.error != null -> EmptyStateCard(
                state.error ?: stringResource(R.string.error_generic),
                modifier = Modifier.padding(16.dp),
            )
            else -> {
                LazyColumn(
                    modifier       = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp), // FAB clearance
                ) {
                    if (pinnedFamilies.isNotEmpty()) {
                        item { SectionHeader(stringResource(R.string.section_pinned)) }
                        items(pinnedFamilies, key = { "pin_${it.id}" }) { family ->
                            FamilyListItem(
                                family      = family,
                                isPinned    = true,
                                isMine      = family.id in state.myFamilyIds,
                                onClick     = { onFamilyClick(family.id) },
                                onPinToggle = { vm.togglePin(family.id) },
                            )
                        }
                    }

                    // ── All families section ──────────────────────────────────
                    if (unpinnedFamilies.isNotEmpty()) {
                        item {
                            SectionHeader(
                                if (pinnedFamilies.isEmpty()) stringResource(R.string.section_all_families)
                                else stringResource(R.string.section_all_families)
                            )
                        }
                        items(unpinnedFamilies, key = { "fam_${it.id}" }) { family ->
                            FamilyListItem(
                                family      = family,
                                isPinned    = false,
                                isMine      = family.id in state.myFamilyIds,
                                onClick     = { onFamilyClick(family.id) },
                                onPinToggle = { vm.togglePin(family.id) },
                            )
                        }
                    }

                    if (pinnedFamilies.isEmpty() && unpinnedFamilies.isEmpty()) {
                        item {
                            EmptyStateCard(
                                stringResource(R.string.families_empty_search),
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
