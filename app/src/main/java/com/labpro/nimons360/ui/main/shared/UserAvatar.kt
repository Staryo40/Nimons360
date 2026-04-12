package com.labpro.nimons360.ui.main.shared

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.labpro.nimons360.ui.theme.PinColors
import com.labpro.nimons360.ui.theme.TextOnDark

@Composable
fun UserAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Int = 36,
    colorIndex: Int = -1,           // -1 = primary teal (current user)
    textColor: Color = TextOnDark,
    onClick: () -> Unit = {},
) {
    val bg = if (colorIndex < 0) MaterialTheme.colorScheme.primary
    else PinColors[colorIndex % PinColors.size]
    val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text       = initial,
            color      = textColor,
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}