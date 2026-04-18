package com.labpro.nimons360.ui.main.shared

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text  = title,
        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.0.sp),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .semantics { heading() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        fontWeight = FontWeight.SemiBold,
    )
}
