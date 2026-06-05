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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.labpro.nimons360.R
import com.labpro.nimons360.data.model.family.Family


import androidx.compose.foundation.layout.Column
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.labpro.nimons360.ui.main.screens.home.MemberAvatarRow

@Composable
fun FamilyListItem(
    family:      Family,
    isPinned:    Boolean,
    isMine:      Boolean,
    onClick:     () -> Unit,
    onPinToggle: () -> Unit,
) {
    val openFamilyDescription = stringResource(R.string.cd_open_family, family.name)
    val pinDescription = stringResource(
        if (isPinned) R.string.cd_unpin_family_named else R.string.cd_pin_family_named,
        family.name,
    )
    
    val resolvedIconUrl = if (!family.iconUrl.isNullOrBlank()) {
        if (family.iconUrl.startsWith("/")) {
            "${com.labpro.nimons360.BuildConfig.BASE_URL}${family.iconUrl}"
        } else {
            family.iconUrl
        }
    } else {
        null
    }

    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = openFamilyDescription
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (resolvedIconUrl != null) {
            AsyncImage(
                model = resolvedIconUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_placeholder_avatar),
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
            )
        } else {
            Box(
                modifier         = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isMine) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.secondaryContainer,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text       = family.name.first().uppercaseChar().toString(),
                    style      = MaterialTheme.typography.titleSmall,
                    color      = if (isMine) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text       = family.name,
                style      = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isMine) FontWeight.SemiBold else FontWeight.Normal,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
            )
            
            val members = family.members
            if (!members.isNullOrEmpty()) {
                MemberAvatarRow(
                    members = members,
                    total = members.size,
                    maxVisible = 4
                )
            }
        }

        // Pin toggle
        IconButton(
            onClick  = onPinToggle,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector        = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                contentDescription = pinDescription,
                tint               = if (isPinned) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
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
