package com.example.tunerapp.utilities

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.example.tunerapp.R


class TunerLineManager(private val context: Context) {
    private val tunerLineViews = mutableMapOf<Int, ImageView>()

    private val nightModeTurnedOn = context.resources.getBoolean(R.bool.is_night_mode)

    private val inactiveColor = if(nightModeTurnedOn) {
        ContextCompat.getColor(context, R.color.tuner_line_inactive_night_mode)
    } else {
        ContextCompat.getColor(context, R.color.tuner_line_inactive)
    }

    private val inPitchColor = ContextCompat.getColor(context, R.color.tuner_line_perfect)
    private val slightOffColor = ContextCompat.getColor(context, R.color.tuner_line_slight_off)
    private val outOfTuneColor = ContextCompat.getColor(context, R.color.tuner_line_very_off)

    fun addLine(cents:Int, imageView: ImageView) {
        tunerLineViews[cents] = imageView
        resetLineColor(imageView)
    }

    fun updateLines(centsOff: Float)  {
        tunerLineViews.values.forEach { resetLineColor(it) }

        val centsOffRounded = (centsOff/10).toInt() * 10
        val closestCents = when {
            centsOffRounded < -50 -> -50
            centsOffRounded > 50 -> 50
            else -> centsOffRounded
        }

        tunerLineViews[closestCents]?.let {
            val color = getColorForCents(closestCents.toFloat())
            setLineColor(it, color)
        }
    }

    private fun resetLineColor(lineView: ImageView) {
        setLineColor(lineView, inactiveColor)
    }

    private fun setLineColor(lineView: ImageView, color: Int) {
        val drawable = lineView.drawable as? GradientDrawable ?: GradientDrawable()
        drawable.setColor(color)
        lineView.setImageDrawable(drawable)
    }

    private fun getColorForCents(cents: Float): Int {
        val absCents = kotlin.math.abs(cents)
        return when {
            absCents < 5 -> inPitchColor
            absCents < 30 -> slightOffColor
            else -> outOfTuneColor
        }
    }
}