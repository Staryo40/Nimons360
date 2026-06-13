package com.labpro.nimons360.ui.features.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.core.content.ContextCompat
import com.labpro.nimons360.R
import java.io.File
import kotlin.math.max

object MapPinMaker {
    fun self(
        context: Context,
        letter: String,
        name: String,
        rotation: Float,
        color: Int,
    ): Drawable {
        val size = dp(context, 54)
        val label = labelSpec(context, name)
        val bitmapWidth = max(size, label.width)
        val bitmapHeight = label.height + size
        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val centerX = bitmapWidth / 2f
        val centerY = label.height + size / 2f

        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.FILL
        }
        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE
            textSize = size * 0.28f
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT_BOLD, android.graphics.Typeface.BOLD)
        }
        val arrow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = ContextCompat.getColor(context, R.color.secondary_coral)
            style = Paint.Style.FILL
        }

        drawLabel(canvas, label, bitmapWidth)
        canvas.drawCircle(centerX, centerY, size * 0.36f, fill)
        val bounds = Rect()
        text.getTextBounds(letter, 0, letter.length, bounds)
        canvas.drawText(letter, centerX, centerY + bounds.height() / 2f, text)

        canvas.save()
        canvas.rotate(rotation, centerX, centerY)
        val path = Path().apply {
            moveTo(centerX, label.height + size * 0.02f)
            lineTo(centerX - size * 0.11f, label.height + size * 0.22f)
            lineTo(centerX + size * 0.11f, label.height + size * 0.22f)
            close()
        }
        canvas.drawPath(path, arrow)
        canvas.restore()

        return BitmapDrawable(context.resources, bitmap)
    }

    fun selfWithBitmap(
        context: Context,
        avatar: Bitmap,
        name: String,
        rotation: Float,
        color: Int,
    ): Drawable {
        val size = dp(context, 54)
        val label = labelSpec(context, name)
        val bitmapWidth = max(size, label.width)
        val bitmapHeight = label.height + size
        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val centerX = bitmapWidth / 2f
        val centerY = label.height + size / 2f
        val radius = size * 0.36f

        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.FILL
        }
        val arrow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = ContextCompat.getColor(context, R.color.secondary_coral)
            style = Paint.Style.FILL
        }

        drawLabel(canvas, label, bitmapWidth)
        canvas.drawCircle(centerX, centerY, radius, fill)

        val innerRadius = radius - dp(context, 2)
        if (innerRadius > 0) {
            val safeAvatar = if (avatar.config == Bitmap.Config.HARDWARE) {
                avatar.copy(Bitmap.Config.ARGB_8888, false)
            } else {
                avatar
            }
            
            val cropped = if (safeAvatar.width != safeAvatar.height) {
                centerCropToSquare(safeAvatar)
            } else {
                safeAvatar
            }
            
            val scaledAvatar = Bitmap.createScaledBitmap(cropped, (innerRadius * 2).toInt(), (innerRadius * 2).toInt(), true)
            
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val clipPath = Path().apply {
                addCircle(centerX, centerY, innerRadius, Path.Direction.CW)
            }
            
            canvas.save()
            canvas.clipPath(clipPath)
            
            canvas.drawBitmap(
                scaledAvatar,
                centerX - innerRadius,
                centerY - innerRadius,
                paint
            )
            
            canvas.restore()
            if (scaledAvatar !== cropped) {
                scaledAvatar.recycle()
            }
            if (cropped !== safeAvatar) {
                cropped.recycle()
            }
            if (safeAvatar !== avatar) {
                safeAvatar.recycle()
            }
        }

        canvas.save()
        canvas.rotate(rotation, centerX, centerY)
        val path = Path().apply {
            moveTo(centerX, label.height + size * 0.02f)
            lineTo(centerX - size * 0.11f, label.height + size * 0.22f)
            lineTo(centerX + size * 0.11f, label.height + size * 0.22f)
            close()
        }
        canvas.drawPath(path, arrow)
        canvas.restore()

        return BitmapDrawable(context.resources, bitmap)
    }

    fun member(
        context: Context,
        letter: String,
        name: String,
        color: Int,
        showLabel: Boolean = true,
    ): Drawable {
        val size = dp(context, 48)
        val label = if (showLabel) labelSpec(context, name) else null
        val labelHeight = label?.height ?: 0
        val bitmapWidth = max(size, label?.width ?: 0)
        val bitmapHeight = labelHeight + size
        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
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
        val centerX = bitmapWidth / 2f
        val centerY = labelHeight + size / 2f
        val bounds = Rect()

        label?.let { drawLabel(canvas, it, bitmapWidth) }
        canvas.drawCircle(centerX, centerY, size * 0.36f, fill)
        text.getTextBounds(letter, 0, letter.length, bounds)
        canvas.drawText(letter, centerX, centerY + bounds.height() / 2f, text)

        return BitmapDrawable(context.resources, bitmap)
    }

    fun memberWithBitmap(
        context: Context,
        avatar: Bitmap,
        name: String,
        color: Int,
        showLabel: Boolean = true,
    ): Drawable {
        val size = dp(context, 48)
        val label = if (showLabel) labelSpec(context, name) else null
        val labelHeight = label?.height ?: 0
        val bitmapWidth = max(size, label?.width ?: 0)
        val bitmapHeight = labelHeight + size
        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        label?.let { drawLabel(canvas, it, bitmapWidth) }

        val centerX = bitmapWidth / 2f
        val centerY = labelHeight + size / 2f
        val radius = size * 0.36f

        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.FILL
        }
        canvas.drawCircle(centerX, centerY, radius, ringPaint)

        val innerRadius = radius - dp(context, 2)
        if (innerRadius > 0) {
            val safeAvatar = if (avatar.config == Bitmap.Config.HARDWARE) {
                avatar.copy(Bitmap.Config.ARGB_8888, false)
            } else {
                avatar
            }
            
            val cropped = if (safeAvatar.width != safeAvatar.height) {
                centerCropToSquare(safeAvatar)
            } else {
                safeAvatar
            }
            
            val scaledAvatar = Bitmap.createScaledBitmap(cropped, (innerRadius * 2).toInt(), (innerRadius * 2).toInt(), true)
            
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val clipPath = Path().apply {
                addCircle(centerX, centerY, innerRadius, Path.Direction.CW)
            }
            
            canvas.save()
            canvas.clipPath(clipPath)
            
            canvas.drawBitmap(
                scaledAvatar,
                centerX - innerRadius,
                centerY - innerRadius,
                paint
            )
            
            canvas.restore()
            if (scaledAvatar !== cropped) {
                scaledAvatar.recycle()
            }
            if (cropped !== safeAvatar) {
                cropped.recycle()
            }
            if (safeAvatar !== avatar) {
                safeAvatar.recycle()
            }
        }

        return BitmapDrawable(context.resources, bitmap)
    }

    private fun centerCropToSquare(bitmap: Bitmap): Bitmap {
        val size = Math.min(bitmap.width, bitmap.height)
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2
        return Bitmap.createBitmap(bitmap, x, y, size, size)
    }

    fun custom(context: Context, file: File, name: String): Drawable? {
        val source = android.graphics.BitmapFactory.decodeFile(file.absolutePath) ?: return null
        val width = dp(context, 54)
        val height = (source.height * (width.toFloat() / source.width))
            .toInt()
            .coerceIn(dp(context, 40), dp(context, 72))
        val scaled = Bitmap.createScaledBitmap(source, width, height, true)
        if (scaled !== source) source.recycle()

        val label = labelSpec(context, name)
        val bitmapWidth = max(width, label.width)
        val bitmap = Bitmap.createBitmap(
            bitmapWidth,
            label.height + height,
            Bitmap.Config.ARGB_8888,
        )
        val canvas = Canvas(bitmap)
        drawLabel(canvas, label, bitmapWidth)
        canvas.drawBitmap(scaled, (bitmapWidth - width) / 2f, label.height.toFloat(), null)
        scaled.recycle()
        return BitmapDrawable(context.resources, bitmap)
    }

    private fun labelSpec(context: Context, rawName: String): LabelSpec {
        val name = rawName.trim()
            .substringBefore(' ')
            .ifBlank { "You" }
            .take(MAX_NAME_LENGTH)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.text_primary)
            textSize = sp(context, 11)
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val horizontalPadding = dp(context, 8)
        return LabelSpec(
            text = name,
            width = (textPaint.measureText(name) + horizontalPadding * 2).toInt()
                .coerceAtLeast(dp(context, 42)),
            height = dp(context, 23),
            paint = textPaint,
            radius = dp(context, 7).toFloat(),
            verticalPadding = dp(context, 3),
        )
    }

    private fun drawLabel(canvas: Canvas, label: LabelSpec, bitmapWidth: Int) {
        val left = (bitmapWidth - label.width) / 2f
        val rect = RectF(left, 0f, left + label.width, label.height - label.verticalPadding.toFloat())
        val background = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            setShadowLayer(2f, 0f, 1f, 0x55000000)
        }
        canvas.drawRoundRect(rect, label.radius, label.radius, background)
        val baseline = rect.centerY() - (label.paint.ascent() + label.paint.descent()) / 2f
        canvas.drawText(label.text, rect.centerX(), baseline, label.paint)
    }

    private fun dp(context: Context, value: Int): Int {
        val density = context.resources.displayMetrics.density
        return (value * density).toInt()
    }

    private fun sp(context: Context, value: Int): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            value.toFloat(),
            context.resources.displayMetrics,
        )
    }

    private data class LabelSpec(
        val text: String,
        val width: Int,
        val height: Int,
        val paint: Paint,
        val radius: Float,
        val verticalPadding: Int,
    )

    private const val MAX_NAME_LENGTH = 12
}
