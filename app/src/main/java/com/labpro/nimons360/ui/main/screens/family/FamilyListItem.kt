package com.labpro.nimons360.ui.main.screens.family

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.labpro.nimons360.data.model.family.Family


@Composable
fun FamilyListItem(
    family:      Family,
    isPinned:    Boolean,
    isMine:      Boolean,
    onClick:     () -> Unit,
    onPinToggle: () -> Unit,
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Icon placeholder — real image via Coil in a later iteration
        Box(
            modifier         = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isMine) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text       = family.name.first().uppercaseChar().toString(),
                style      = MaterialTheme.typography.titleSmall,
                color      = if (isMine) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
        }

        Text(
            text       = family.name,
            style      = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isMine) FontWeight.SemiBold else FontWeight.Normal,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis,
            modifier   = Modifier.weight(1f),
        )

        // Pin toggle
        IconButton(
            onClick  = onPinToggle,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector        = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                contentDescription = if (isPinned) "Unpin family" else "Pin family",
                tint               = if (isPinned) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(20.dp),
            )
        }
    }

    HorizontalDivider(
        modifier  = Modifier.padding(start = 72.dp),
        thickness = 0.5.dp,
        color     = MaterialTheme.colorScheme.outline,
    )
}
