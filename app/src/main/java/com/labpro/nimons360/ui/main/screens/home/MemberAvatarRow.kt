package com.labpro.nimons360.ui.main.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.labpro.nimons360.R
import com.labpro.nimons360.data.model.family.FamilyMember
import com.labpro.nimons360.ui.main.shared.UserAvatar

@Composable
fun MemberAvatarRow(
    members: List<FamilyMember>,
    total: Int,
    maxVisible: Int = 3,
) {
    val visibleNames = members.take(maxVisible).joinToString(", ") { it.fullName }
    val summary = if (total > maxVisible) {
        "$visibleNames, ${stringResource(R.string.a11y_more_members, total - maxVisible)}"
    } else {
        visibleNames
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy((-8).dp),
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = stringResource(R.string.a11y_member_row, summary)
        },
    ) {
        members.take(maxVisible).forEachIndexed { index, member ->
            UserAvatar(
                name       = member.fullName,
                size       = 28,
                colorIndex = index,
                contentDescription = null,
                modifier   = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surface)
                    .clearAndSetSemantics { },
            )
        }
        if (total > maxVisible) {
            val overflowCount = total - maxVisible
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary)
                    .clearAndSetSemantics { },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text  = "+$overflowCount",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}
