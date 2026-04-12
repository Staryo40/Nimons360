package com.labpro.nimons360.ui.main.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.labpro.nimons360.MainApplication
import com.labpro.nimons360.data.model.family.DiscoverFamily
import com.labpro.nimons360.data.model.family.FamilyMember
import com.labpro.nimons360.data.model.family.FamilyWithMembers
import com.labpro.nimons360.ui.main.shared.EmptyStateCard
import com.labpro.nimons360.ui.main.shared.LoadingSection
import com.labpro.nimons360.ui.main.shared.RowDivider
import com.labpro.nimons360.ui.main.shared.SectionHeader
import com.labpro.nimons360.ui.main.shared.UserAvatar
import com.labpro.nimons360.viewmodel.HomeViewModel
import com.labpro.nimons360.viewmodel.HomeViewModelFactory
import com.labpro.nimons360.data.model.user.UserData
import kotlin.collections.take

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
                    contentPadding    = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 8.dp),
                ) {
                    items(state.myFamilies) { family ->
                        MyFamilyCard(family = family, onClick = { /* navigate to detail */ })
                    }
                }
            }
        }

        item { SectionHeader("DISCOVER FAMILIES") }

        when {
            state.isLoadingDiscover -> item { LoadingSection() }
            state.discoverFamilies.isEmpty() -> item {
                EmptyStateCard("No new families to discover right now.")
            }
            else -> {
                items(state.discoverFamilies) { family ->
                    DiscoverFamilyItem(
                        family  = family,
                        onJoin  = { /* navigate to detail */ },
                    )
                    RowDivider()
                }
            }
        }
    }
}


@Composable
private fun MyFamilyCard(
    family: FamilyWithMembers,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Family icon placeholder circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text  = family.name.first().uppercaseChar().toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                )
            }

            Text(
                text      = family.name,
                style     = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines  = 1,
                overflow  = TextOverflow.Ellipsis,
            )

            Text(
                text  = "${family.members.size} member${if (family.members.size != 1) "s" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Member avatar stack (max 3 + overflow)
            MemberAvatarRow(members = family.members, total = family.members.size)
        }
    }
}

// ── Discover family row ───────────────────────────────────────────────────────

@Composable
private fun DiscoverFamilyItem(
    family: DiscoverFamily,
    onJoin: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onJoin)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Icon placeholder
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text  = family.name.first().uppercaseChar().toString(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
            )
        }

        // Name + member avatars
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text      = family.name,
                style     = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines  = 1,
                overflow  = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            MemberAvatarRow(members = family.members, total = family.members.size)
        }

        // Join button (secondary coral)
        Button(
            onClick  = onJoin,
            colors   = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor   = MaterialTheme.colorScheme.onSecondary,
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            modifier = Modifier.height(34.dp),
        ) {
            Text("Join", style = MaterialTheme.typography.labelMedium)
        }
    }
}

// ── Stacked member avatars ────────────────────────────────────────────────────

@Composable
private fun MemberAvatarRow(
    members: List<FamilyMember>,
    total: Int,
    maxVisible: Int = 3,
) {
    Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
        members.take(maxVisible).forEachIndexed { index, member ->
            UserAvatar(
                name       = member.fullName,
                size       = 24,
                colorIndex = index,
                modifier   = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surface),
            )
        }
        if (total > maxVisible) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.outline),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text  = "+${total - maxVisible}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.surface,
                )
            }
        }
    }
}