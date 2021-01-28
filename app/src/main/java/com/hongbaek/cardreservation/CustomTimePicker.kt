package com.hongbaek.cardreservation

import android.annotation.SuppressLint
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.widget.NumberPicker
import android.widget.TimePicker
import androidx.annotation.IntRange
import androidx.core.math.MathUtils


class CustomTimePicker @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
) : TimePicker(context, attrs) {
    private val defaultInterval = 5
    val minInterval = 1
    val maxInterval = 30

    var timeInterval = defaultInterval
        set(value) {
            if (field !in minInterval..maxInterval) {
                Log.w("RangeTimePicker", "timeInterval must be between $minInterval..$maxInterval")
            }

            field = MathUtils.clamp(minInterval, maxInterval, value)
            setInterval(field)
            invalidate()
        }

    init {
        setInterval()
    }

    @SuppressLint("PrivateApi")
    fun setInterval(
            @IntRange(from = 1, to = 30)
            timeInterval: Int = defaultInterval
    ) {
        try {
            val classForId = Class.forName("com.android.internal.R\$id")
            val fieldId = classForId.getField("minute").getInt(null)
            (this.findViewById(fieldId) as NumberPicker).apply {
                minValue = 0
                maxValue = 60 / timeInterval - 1
                displayedValues = getDisplayedValue()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getDisplayedMinutes(): Int {
        return if(Build.VERSION.SDK_INT >=23) minute * timeInterval
        else currentMinute * timeInterval
    }
    private fun getDisplayedValue(
            interval: Int = timeInterval
    ): Array<String> {
        val minutesArray = ArrayList<String>()
        for (i in 0 until 60 step interval) {
            minutesArray.add("$i")
        }
        return minutesArray.toArray(arrayOf(""))
    }

}