package com.labpro.nimons360.ui.theme

import androidx.compose.ui.graphics.Color

// ── Primary Brand ─────────────────────────────────────────────────────────────
val PrimaryTeal        = Color(0xFF006D77)
val PrimaryTealDark    = Color(0xFF004E56)
val PrimaryTealLight   = Color(0xFF339DA5)
val OnPrimary          = Color(0xFFFFFFFF)

// ── Secondary / Accent ────────────────────────────────────────────────────────
val SecondaryCoral     = Color(0xFFE29578)
val SecondaryCoralDark = Color(0xFFB96E4E)
val OnSecondary        = Color(0xFFFFFFFF)

// ── Backgrounds & Surfaces ────────────────────────────────────────────────────
/** Main page background (Home, Families, Profile) */
val BackgroundBase     = Color(0xFFF4F6F8)
/** Cards, bottom sheets, pop-ups */
val SurfaceWhite       = Color(0xFFFFFFFF)

// ── Text ──────────────────────────────────────────────────────────────────────
/** Headings, family names, primary content */
val TextPrimary        = Color(0xFF1D3557)
/** Emails, timestamps, secondary info */
val TextSecondary      = Color(0xFF6C757D)
/** On dark surfaces (inside teal header) */
val TextOnDark         = Color(0xFFFFFFFF)

// ── Semantic / Status ─────────────────────────────────────────────────────────
/** High battery, Wi-Fi status, success */
val SuccessMint        = Color(0xFF2A9D8F)
/** Sign Out, Leave Family, errors, disconnected */
val DangerCrimson      = Color(0xFFD62828)
/** Low battery, "Not a member" */
val CautionSaffron     = Color(0xFFE9C46A)
/** Mobile data, info dialogues */
val InfoMutedBlue      = Color(0xFF457B9D)

// ── Utility ───────────────────────────────────────────────────────────────────
val Divider            = Color(0xFFE0E4E8)
val DisabledBg         = Color(0xFFC8CDD3)
val DisabledText       = Color(0xFF9EA6AE)

// ── Map pin avatar palette ────────────────────────────────────────────────────
val PinColors = listOf(
    Color(0xFFE63946),  // red
    Color(0xFF2A9D8F),  // mint
    Color(0xFF457B9D),  // blue
    Color(0xFFE29578),  // coral
    Color(0xFF6C4AB6),  // purple
)