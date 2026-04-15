package com.labpro.nimons360.ui.features.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.labpro.nimons360.R

object MapPinMaker {
    fun self(context: Context, letter: String, rotation: Float): Drawable {
        val size = dp(context, 54)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val center = size / 2f

        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.primary_teal)
            style = Paint.Style.FILL
        }
        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = size * 0.28f
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT_BOLD, android.graphics.Typeface.BOLD)
        }
        val arrow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.secondary_coral)
            style = Paint.Style.FILL
        }

        canvas.drawCircle(center, center, size * 0.36f, fill)
        val bounds = Rect()
        text.getTextBounds(letter, 0, letter.length, bounds)
        canvas.drawText(letter, center, center + bounds.height() / 2f, text)

        canvas.save()
        canvas.rotate(rotation, center, center)
        val path = Path().apply {
            moveTo(center, size * 0.02f)
            lineTo(center - size * 0.11f, size * 0.22f)
            lineTo(center + size * 0.11f, size * 0.22f)
            close()
        }
        canvas.drawPath(path, arrow)
        canvas.restore()

        return BitmapDrawable(context.resources, bitmap)
    }

    fun member(context: Context, letter: String, color: Int): Drawable {
        val size = dp(context, 48)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.FILL
        }
        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE
            textSize = size * 0.32f
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT_BOLD, android.graphics.Typeface.BOLD)
        }
        val center = size / 2f
        val bounds = Rect()

        canvas.drawCircle(center, center, size * 0.36f, fill)
        text.getTextBounds(letter, 0, letter.length, bounds)
        canvas.drawText(letter, center, center + bounds.height() / 2f, text)

        return BitmapDrawable(context.resources, bitmap)
    }

    private fun dp(context: Context, value: Int): Int {
        val density = context.resources.displayMetrics.density
        return (value * density).toInt()
    }
}
