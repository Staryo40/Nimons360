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
import androidx.compose.ui.unit.dp
import com.labpro.nimons360.data.model.family.FamilyMember
import com.labpro.nimons360.ui.main.shared.UserAvatar

@Composable
fun MemberAvatarRow(
    members: List<FamilyMember>,
    total: Int,
    maxVisible: Int = 3,
) {
    Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
        members.take(maxVisible).forEachIndexed { index, member ->
            UserAvatar(
                name       = member.fullName,
                size       = 28,
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
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.surface,
                )
            }
        }
    }
}