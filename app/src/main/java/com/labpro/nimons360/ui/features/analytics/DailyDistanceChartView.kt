package com.labpro.nimons360.ui.features.analytics

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.labpro.nimons360.R
import com.labpro.nimons360.data.repository.DailyDistance
import kotlin.math.max

class DailyDistanceChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.analytics_bar)
    }
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.analytics_blue)
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.text_secondary)
        textSize = resources.getDimension(R.dimen.analytics_chart_label)
        textAlign = Paint.Align.CENTER
    }

    private var values: List<DailyDistance> = emptyList()
    private var highlightedDay: Int = 0

    fun submit(items: List<DailyDistance>, highlightDay: Int) {
        values = items
        highlightedDay = highlightDay
        contentDescription = context.getString(
            R.string.analytics_chart_description,
            items.sumOf(DailyDistance::distanceKm),
        )
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (values.isEmpty()) return

        val density = resources.displayMetrics.density
        val chartTop = paddingTop.toFloat()
        val chartBottom = height - paddingBottom - 22f * density
        val chartHeight = max(1f, chartBottom - chartTop)
        val slotWidth = (width - paddingLeft - paddingRight).toFloat() / values.size
        val barWidth = max(4f * density, slotWidth * 0.62f)
        val maximum = values.maxOfOrNull(DailyDistance::distanceKm)?.coerceAtLeast(0.1) ?: 0.1

        values.forEachIndexed { index, item ->
            val center = paddingLeft + slotWidth * index + slotWidth / 2
            val normalized = (item.distanceKm / maximum).toFloat()
            val visibleRatio = if (item.distanceKm > 0.0) normalized.coerceAtLeast(0.08f) else 0.025f
            val top = chartBottom - chartHeight * visibleRatio
            val paint = if (item.date.dayOfMonth == highlightedDay) highlightPaint else barPaint
            canvas.drawRoundRect(
                center - barWidth / 2,
                top,
                center + barWidth / 2,
                chartBottom,
                3f * density,
                3f * density,
                paint,
            )

            val day = item.date.dayOfMonth
            if (day == 1 || day == values.size || day % 5 == 0) {
                canvas.drawText(
                    day.toString(),
                    center,
                    height - paddingBottom.toFloat(),
                    labelPaint,
                )
            }
        }
    }
}
