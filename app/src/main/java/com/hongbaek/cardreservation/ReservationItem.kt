package com.hongbaek.cardreservation

import android.util.Log
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class ReservationItem {
    var userName : String? = null
    var addedTime : String? = null
    var uid : String? = null
    lateinit var startTime : String
    lateinit var endTime : String
    var calStartTime: Calendar
    var calEndTime: Calendar
    lateinit var calDayStart:CalendarDay
    lateinit var calDayEnd: CalendarDay
    var tempTime: Timestamp = Timestamp(1610436974000)
    init{
        var c = Calendar.getInstance()
        c.time = Date(tempTime.time)
        Log.d("Timestamp TEST", "Timestamp:  ${c.toString()}")
        calStartTime = Calendar.getInstance()
        Log.d("Timestamp TEST", "startTimestamp: ${calStartTime.time}")
        calEndTime = Calendar.getInstance()
        calEndTime.add(Calendar.HOUR_OF_DAY, 6)
        timeConvertToString()
    }
    fun adaptTime(){
        timeConvertToCalender()
        timeConvertToString()
    }

    fun timeConvertToCalender():Boolean{
        try {
            val df = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA)
            calStartTime = Calendar.getInstance()
            calStartTime.time = df.parse(startTime) ?: calStartTime.time
            calEndTime  = Calendar.getInstance()
            calEndTime.time = df.parse(endTime) ?: calEndTime.time
            calendarToCalendarDay()
            return true
        }catch (e:Exception){
            Log.e("timeConvertToCalender", e.message?:"null")
            e.printStackTrace()
            return false
        }
    }
    fun calendarToCalendarDay(){
        calDayStart = CalendarDay.from(calStartTime.get(Calendar.YEAR), calStartTime.get(Calendar.MONTH), calStartTime.get(Calendar.DAY_OF_MONTH))
        calDayEnd = CalendarDay.from(calEndTime.get(Calendar.YEAR), calEndTime.get(Calendar.MONTH), calEndTime.get(Calendar.DAY_OF_MONTH))
    }
    fun calendarDayToCalendar(){
    }
    fun timeConvertToString():Boolean{
        val df = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA)
        startTime = df.format(calStartTime.time)
        endTime = df.format(calEndTime.time)
        return true
    }
}