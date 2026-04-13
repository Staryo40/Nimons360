package com.labpro.nimons360.ui.main.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.labpro.nimons360.data.model.family.DiscoverFamily

@Composable
fun DiscoverFamilyItem(
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