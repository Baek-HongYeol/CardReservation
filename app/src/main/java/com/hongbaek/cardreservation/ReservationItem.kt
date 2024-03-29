package com.hongbaek.cardreservation

import android.util.Log
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class ReservationItem {
    var userName : String? = null
    var title : String? = null
    var addedTime : String? = null
    var uid : String? = null
    var estimated: Int = 0
    lateinit var startTime : String
    lateinit var endTime : String
    var status: STATUS = STATUS.default

    var calStartTime: Calendar
    var calEndTime: Calendar
    var tempTime: Timestamp = Timestamp(1610540083000)
    init{
        var c = Calendar.getInstance()
        c.time = Date(tempTime.time)
        calStartTime = Calendar.getInstance()
        calEndTime = Calendar.getInstance()
        calStartTime.timeInMillis=0
        calEndTime.timeInMillis=0
        timeConvertToString()
    }
    fun adaptTime(){
        timeConvertToCalendar()
        timeConvertToString()
    }

    fun timeConvertToCalendar():Boolean{
        try {
            val df = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA)
            calStartTime.time = df.parse(startTime) ?: calStartTime.time
            Log.d("ReservationItem", "timeToCalendar-startTime_$startTime")
            Log.d("ReservationItem", "timeToCalendar-carStartTime_${calStartTime.get(Calendar.DAY_OF_MONTH)}")
            calEndTime.time = df.parse(endTime) ?: calEndTime.time
            Log.d("ReservationItem", "timeToCalendar-endTime_$endTime")
            Log.d("ReservationItem", "timeToCalendar-carEndTime_${calEndTime.get(Calendar.DAY_OF_MONTH)}")
            return true
        }catch (e:Exception){
            Log.e("timeConvertToCalender", e.message?:"null")
            e.printStackTrace()
            return false
        }
    }
    fun timeConvertToString():Boolean{
        val df = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA)
        startTime = df.format(calStartTime.time)
        endTime = df.format(calEndTime.time)
        return true
    }

    fun isStatus(mStatus:String){
        try{
            status = STATUS.valueOf(mStatus)
        }catch(e:Exception){
            status = STATUS.default
        }
    }

    enum class STATUS(status:String?) {
        RESERVED("RESERVED"){
            override fun toString(): String = "예약됨"
        },
        RUNNING("RUNNING"){
            override fun toString(): String = "진행중"
        },
        COMPLETED("COMPLETED"){
            override fun toString(): String = "시간종료"
        },
        RETURNED("RETURNED"){
            override fun toString(): String = "반납완료"
        },
        UNKNOWN("UNKNOWN"){
            override fun toString(): String = ""
        };

        companion object {
            val default:STATUS
                get() = UNKNOWN
        }

        abstract override fun toString():String
    }

}