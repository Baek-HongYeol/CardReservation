package com.hongbaek.cardreservation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.collections.ArrayList

class CalendarViewModel: ViewModel() {

    /*var centerPosition:Int = 0

    fun setCalendarList(){

        var list: ArrayList<CalendarDay> = ArrayList()
        var cal: GregorianCalendar = GregorianCalendar()
        for (i in -300..299) {
            try {
                val calendar = GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + i, 1, 0, 0, 0)
                if (i == 0) {
                    centerPosition = list.size
                }
                val emptydays: Int = calendar.get(Calendar.DAY_OF_WEEK) - 1 //해당 월에 시작하는 요일 -1
                val max: Int = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) // 해당 월에 마지막 날
                val lateemptydays: Int = (7-((max-28)+emptydays))%7

                // EMPTY 생성
                for (j in 0 until emptydays) {
                    list.add(CalendarDay(calendar, true))
                }
                for (j in 1..max) {
                    list.add(CalendarDay(GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), j)))
                }
                for (j in 0 until lateemptydays)
                    list.add(CalendarDay(calendar, true))

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        calendarList.value = list
    }*/

}