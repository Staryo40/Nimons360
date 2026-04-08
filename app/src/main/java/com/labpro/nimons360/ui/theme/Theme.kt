package com.labpro.nimons360.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// ── Light colour scheme ───────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    // Primary – Deep Teal
    primary             = PrimaryTeal,
    onPrimary           = OnPrimary,
    primaryContainer    = PrimaryTealLight,
    onPrimaryContainer  = TextPrimary,

    // Secondary – Soft Coral
    secondary            = SecondaryCoral,
    onSecondary          = OnSecondary,
    secondaryContainer   = SecondaryCoral.copy(alpha = 0.20f),
    onSecondaryContainer = SecondaryCoralDark,

    // Tertiary – Info Blue (reused for additional brand moments)
    tertiary            = InfoMutedBlue,
    onTertiary          = OnPrimary,

    // Backgrounds & surfaces
    background          = BackgroundBase,
    onBackground        = TextPrimary,
    surface             = SurfaceWhite,
    onSurface           = TextPrimary,
    onSurfaceVariant    = TextSecondary,

    // Outlines & dividers
    outline             = Divider,
    outlineVariant      = DisabledBg,

    // Error / destructive
    error               = DangerCrimson,
    onError             = OnPrimary,
    errorContainer      = DangerCrimson.copy(alpha = 0.12f),
    onErrorContainer    = DangerCrimson,

    // Inverse (used by snackbars)
    inverseSurface      = TextPrimary,
    inverseOnSurface    = SurfaceWhite,
    inversePrimary      = PrimaryTealLight,

    // Scrim for modal overlays
    scrim               = TextPrimary.copy(alpha = 0.38f),
)

/**
 * Nimons360 Compose theme.
 *
 * Wrap every Composable entry-point with this theme so colour, typography,
 * and shape tokens are consistently applied throughout the app.
 *
 * Adding `darkTheme` support later is as simple as creating a
 * `darkColorScheme` and passing it via the `darkTheme` branch below.
 */
@Composable
fun Nimons360Theme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = Typography,
        content     = content,
    )
}