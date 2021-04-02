package com.hongbaek.cardreservation

import android.content.Context
import android.os.Build
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.TimePicker
import androidx.core.view.get
import java.util.*

class DateTimePicker(var datePicker: Pair<DatePicker, DatePicker>, var timePicker: Pair<CustomTimePicker, CustomTimePicker>) {
    var onDateChangedListener: DatePicker.OnDateChangedListener = DatePicker.OnDateChangedListener { view, year, monthOfYear, dayOfMonth ->  }
        set(value) {
            field = value
            datePicker.first.init(datePicker.first.year, datePicker.first.month, datePicker.first.dayOfMonth, value)
            datePicker.second.init(datePicker.second.year, datePicker.second.month, datePicker.second.dayOfMonth, value)
        }
    var onTimeChangedListener: TimePicker.OnTimeChangedListener = TimePicker.OnTimeChangedListener { view, hourOfDay, minute ->  }
        set(value) {
            field = value
            timePicker.first.setOnTimeChangedListener(value)
            timePicker.second.setOnTimeChangedListener(value)
        }

    var date : Calendar = Calendar.getInstance()
        get() {
            date.set(datePicker.first.year, datePicker.first.month-1, datePicker.first.dayOfMonth)
            return date
        }
        set(value){
            var d = Calendar.getInstance()
            d.timeInMillis = 0
            d.set(value.get(Calendar.YEAR), value.get(Calendar.MONTH), value.get(Calendar.DAY_OF_MONTH))
            field = d
        }

    fun init(year:Int, month:Int, day:Int, hour:Int, min:Int, ){
        var today = Calendar.getInstance()
        datePicker.first.minDate = today.timeInMillis
        datePicker.first.updateDate(year, month-1, day)
        datePicker.second.minDate = today.timeInMillis
        datePicker.second.updateDate(year, month-1, day)
        today.add(Calendar.DAY_OF_MONTH, 31)
        datePicker.first.maxDate = today.timeInMillis
        datePicker.second.maxDate = today.timeInMillis
        timePicker.first.setIs24HourView(true)
        timePicker.second.setIs24HourView(true)
        if(Build.VERSION.SDK_INT >= 23){
            timePicker.first.hour = hour
            timePicker.first.minute = min
            timePicker.second.hour = hour
            timePicker.second.minute = min
        }else{
            timePicker.first.currentHour = hour
            timePicker.first.currentMinute = min
            timePicker.second.currentHour = hour
            timePicker.second.currentMinute = min
        }

    }
    fun getTimes(): Pair<Pair<Int, Int>, Pair<Int,Int>>{
        val context = timePicker.first.context
        return if(Build.VERSION.SDK_INT >= 23){
            Pair(Pair(timePicker.first.hour, timePicker.first.getDisplayedMinutes()),
                Pair(timePicker.second.hour, timePicker.second.getDisplayedMinutes()))
        }else{
            Pair(Pair(timePicker.first.currentHour, timePicker.first.getDisplayedMinutes()),
                Pair(timePicker.second.currentHour, timePicker.second.getDisplayedMinutes()))
        }
    }

}