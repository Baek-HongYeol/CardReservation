package com.hongbaek.cardreservation.utils

import android.graphics.Color
import android.text.style.ForegroundColorSpan
import android.util.Log
import com.prolificinteractive.materialcalendarview.*
import java.util.*

class SundayDecorator(val calendarView: MaterialCalendarView):DayViewDecorator {
    private val calendar = Calendar.getInstance()
    private var monthset:Int = -1

    override fun shouldDecorate(day: CalendarDay?): Boolean {
        if(day==null) return false
        day.copyTo(calendar)
        val weekDay = calendar.get(Calendar.DAY_OF_WEEK)
        val month = calendarView.selectedDate?.month?:-1
        return weekDay == Calendar.SUNDAY && day.month == monthset
    }
    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(object: ForegroundColorSpan(Color.RED){})
    }

    fun onMonthChanged(month: Int){
        if(month<1 || month>12)
            return
        monthset = month
    }
}

private fun CalendarDay.copyTo(calendar: Calendar) {
    Log.d("copyTo", "${year}.${month}.${day}")
    calendar.set(this.year, this.month-1, this.day)
    Log.d("copyTo", "${calendar.get(Calendar.DAY_OF_WEEK)}")
}
