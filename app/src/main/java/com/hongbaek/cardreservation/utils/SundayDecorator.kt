package com.hongbaek.cardreservation.utils

import android.graphics.Color
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.core.view.get
import com.prolificinteractive.materialcalendarview.*
import java.util.*

class SundayDecorator(val calendarView: MaterialCalendarView):DayViewDecorator {
    private val calendar = Calendar.getInstance()
    override fun shouldDecorate(day: CalendarDay?): Boolean {
        if(day==null) return false
        day.copyTo(calendar)
        val weekDay = calendar.get(Calendar.DAY_OF_WEEK)
        val month = calendarView.selectedDate?.month?:-1

        return weekDay == Calendar.SUNDAY && day.month == month
    }
    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(object: ForegroundColorSpan(Color.RED){})
    }
}

private fun CalendarDay.copyTo(calendar: Calendar) {
    Log.d("copyTo", "${year}.${month}.${day}")
    calendar.set(this.year, this.month-1, this.day)
    Log.d("copyTo", "${calendar.get(Calendar.DAY_OF_WEEK)}")
}
