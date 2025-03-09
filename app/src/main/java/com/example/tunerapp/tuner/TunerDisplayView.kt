package com.example.tunerapp.tuner

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.example.tunerapp.R
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class TunerDisplayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 24f
        color = Color.GRAY
    }

    private val rectF = RectF()
    private val centerX: Float
        get() = width / 2f
    private val centerY: Float
        get() = height * 0.5f

    private val numberOfLines = 7
    private val lineWidth = 12f
    private val lineSpacing = 15f // Space between lines
    private val archRadius get() = min(width, height * 2) * 0.4f

    private val indicatorPath = Path()
    private val startAngle = 180f
    private val sweepAngle = 180f
    private val spaceBetweenLines = sweepAngle / (numberOfLines - 1)

    // Colors for the indicator lines
    private val perfectPitchColor = Color.rgb(76, 175, 80)  // Green
    private val slightlyOffColor = Color.rgb(255, 235, 59)  // Yellow
    private val veryOffColor = Color.rgb(244, 67, 54)      // Red

    // Neutral color for inactive lines
    private var inactiveLineColor = Color.LTGRAY
    private var textColor = Color.DKGRAY

    // Animation properties
    private var currentCentsOff = 0f
    private var targetCentsOff = 0f
    private var animator: ValueAnimator? = null

    init {
        // Apply theme-specific colors
        val isNightMode = context.resources.getBoolean(R.bool.is_night_mode)
        if (isNightMode) {
            inactiveLineColor = Color.DKGRAY
            textColor = Color.LTGRAY
        }

        textPaint.color = textColor

        // Set up hardware acceleration layer
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    fun setCentsOff(cents: Float) {
        targetCentsOff = cents

        // Cancel any running animation
        animator?.cancel()

        // Create smooth animation for needle movement
        animator = ValueAnimator.ofFloat(currentCentsOff, targetCentsOff).apply {
            duration = 150
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                currentCentsOff = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Update text size based on view dimensions
        textPaint.textSize = min(w, h) * 0.05f
        linePaint.strokeWidth = min(w, h) * 0.01f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw scale labels
        drawScaleLabels(canvas)

        // Draw the arch with indicator lines
        drawArchLines(canvas)

        // Draw needle indicator
        drawNeedleIndicator(canvas)
    }

    private fun drawScaleLabels(canvas: Canvas) {
        // Draw the scale labels for cents deviation
        val labelRadius = archRadius + 40f
        canvas.drawText("-50", centerX - labelRadius * 0.8f, centerY, textPaint)
        canvas.drawText("0", centerX, centerY - 20f, textPaint)
        canvas.drawText("+50", centerX + labelRadius * 0.8f, centerY, textPaint)
    }

    private fun drawArchLines(canvas: Canvas) {
        // Draw each line of the arch with gradually changing colors
        for (i in 0 until numberOfLines) {
            val angle = startAngle + (i * spaceBetweenLines)

            // Calculate cents value for this line
            val lineCents = -50f + (i * (100f / (numberOfLines - 1)))

            // Determine if this line should be highlighted
            val isHighlighted = abs(currentCentsOff - lineCents) < 10

            // Calculate angle in radians
            val radians = Math.toRadians(angle.toDouble())

            // Calculate line position
            val lineLength = if (isHighlighted) 60f else 40f
            val startX = centerX + (archRadius - lineLength) * cos(radians).toFloat()
            val startY = centerY + (archRadius - lineLength) * sin(radians).toFloat()
            val endX = centerX + archRadius * cos(radians).toFloat()
            val endY = centerY + archRadius * sin(radians).toFloat()

            // Set color based on position in the scale
            linePaint.color = if (isHighlighted) {
                getColorForCents(lineCents)
            } else {
                inactiveLineColor
            }

            // Draw the line
            canvas.drawLine(startX, startY, endX, endY, linePaint)
        }
    }

    private fun drawNeedleIndicator(canvas: Canvas) {
        // Map the current cents to an angle
        val centsLimited = currentCentsOff.coerceIn(-50f, 50f)
        val needleAngle = startAngle + ((centsLimited + 50) / 100f * sweepAngle)

        // Set up the needle paint
        linePaint.color = getColorForCents(centsLimited)
        linePaint.strokeWidth = lineWidth * 1.2f

        // Calculate needle position
        val radians = Math.toRadians(needleAngle.toDouble())
        val needleLength = archRadius * 0.9f
        val endX = centerX + needleLength * cos(radians).toFloat()
        val endY = centerY + needleLength * sin(radians).toFloat()

        // Draw the needle
        canvas.drawLine(centerX, centerY, endX, endY, linePaint)

        // Draw a small circle at the pivot point
        canvas.drawCircle(centerX, centerY, lineWidth, linePaint)
    }

    private fun getColorForCents(cents: Float): Int {
        val absCents = abs(cents)
        return when {
            absCents < 5 -> perfectPitchColor
            absCents < 25 -> interpolateColor(perfectPitchColor, slightlyOffColor, (absCents - 5) / 20f)
            else -> interpolateColor(slightlyOffColor, veryOffColor, (absCents - 25) / 25f)
        }
    }

    private fun interpolateColor(color1: Int, color2: Int, fraction: Float): Int {
        val r1 = Color.red(color1)
        val g1 = Color.green(color1)
        val b1 = Color.blue(color1)

        val r2 = Color.red(color2)
        val g2 = Color.green(color2)
        val b2 = Color.blue(color2)

        val r = (r1 + (r2 - r1) * fraction).toInt().coerceIn(0, 255)
        val g = (g1 + (g2 - g1) * fraction).toInt().coerceIn(0, 255)
        val b = (b1 + (b2 - b1) * fraction).toInt().coerceIn(0, 255)

        return Color.rgb(r, g, b)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 400
        val desiredHeight = 300

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(desiredWidth, widthSize)
            else -> desiredWidth
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(desiredHeight, heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(width, height)
    }
}