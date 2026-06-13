package com.labpro.nimons360.ui.theme

import androidx.compose.ui.graphics.Color

// ── Primary Brand ─────────────────────────────────────────────────────────────
val PrimaryTeal        = Color(0xFF006D77)
val PrimaryTealDark    = Color(0xFF004E56)
val PrimaryTealLight   = Color(0xFF2C8B93)
val OnPrimary          = Color(0xFFFFFFFF)

// ── Secondary / Accent ────────────────────────────────────────────────────────
val SecondaryCoral     = Color(0xFFB96E4E)
val SecondaryCoralDark = Color(0xFF8A5038)
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
val TextSecondary      = Color(0xFF4F5963)
/** On dark surfaces (inside teal header) */
val TextOnDark         = Color(0xFFFFFFFF)

// ── Semantic / Status ─────────────────────────────────────────────────────────
/** High battery, Wi-Fi status, success */
val SuccessMint        = Color(0xFF17675F)
/** Sign Out, Leave Family, errors, disconnected */
val DangerCrimson      = Color(0xFFB42318)
/** Low battery, "Not a member" */
val CautionSaffron     = Color(0xFFB78316)
/** Mobile data, info dialogues */
val InfoMutedBlue      = Color(0xFF457B9D)

// ── Utility ───────────────────────────────────────────────────────────────────
val Divider            = Color(0xFFBCC5CC)
val DisabledBg         = Color(0xFFAAB4BD)
val DisabledText       = Color(0xFF7C8792)

// ── Map pin avatar palette ────────────────────────────────────────────────────
val PinColors = listOf(
    Color(0xFFB42318),  // red
    Color(0xFF17675F),  // mint
    Color(0xFF2F607D),  // blue
    Color(0xFF8A5038),  // coral
    Color(0xFF5B3FA3),  // purple
)
