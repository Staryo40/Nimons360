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
        val statusBarTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
        val addedTop = statusBarTop + extraTop
        view.setPadding(
            initialLeft,
            initialTop + addedTop,
            initialRight,
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
