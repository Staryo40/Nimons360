package com.labpro.nimons360.core.utils

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun View.applyStatusBarHeaderInset(extraTopDp: Int = 6) {
    val initialHeight = layoutParams.height
    val initialLeft = paddingLeft
    val initialTop = paddingTop
    val initialRight = paddingRight
    val initialBottom = paddingBottom
    val extraTop = (extraTopDp * resources.displayMetrics.density).toInt()

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val safeInsets = insets.getInsets(
            WindowInsetsCompat.Type.statusBars() or
                WindowInsetsCompat.Type.displayCutout(),
        )
        val symmetricHorizontalInset = maxOf(safeInsets.left, safeInsets.right)
        val addedTop = safeInsets.top + extraTop
        view.setPadding(
            initialLeft + symmetricHorizontalInset,
            initialTop + addedTop,
            initialRight + symmetricHorizontalInset,
            initialBottom,
        )
        if (initialHeight > 0) {
            view.layoutParams = view.layoutParams.apply {
                height = initialHeight + addedTop
            }
        }
        insets
    }
    ViewCompat.requestApplyInsets(this)
}
