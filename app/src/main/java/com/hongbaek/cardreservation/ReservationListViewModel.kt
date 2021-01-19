package com.hongbaek.cardreservation

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.util.*
import kotlin.collections.ArrayList


class ReservationListViewModel(val cardID:String) : ViewModel() {
    class Factory(private val _cardID:String) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            Log.d("RLVM Factory-cardID", _cardID)
            return ReservationListViewModel(_cardID) as T
        }
    }
    var reservationList: MutableLiveData<ArrayList<ReservationItem>> = MutableLiveData()

    private val database = FirebaseDatabase.getInstance()
    private var ref:DatabaseReference
    private var query: Query
    private var isQueryAvailable:Boolean = false
    private val valueEventListener = object : ChildEventListener {
        private val TAG = "RealTimeDB/Reservation/$cardID"
        override fun onCancelled(error: DatabaseError) {
            // Failed to read value
            Log.w(TAG, "Failed to read value."+error.message, error.toException())
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) { }

        override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
            Log.d("RLA_onChildChanged", previousChildName?:"null")
            try{
                val changed : ReservationItem? = dataSnapshot.getValue<ReservationItem>()
                val list = reservationList.value!!
                for(item in list){
                    if(item.addedTime == changed?.addedTime){
                        Log.d("RLVM_onChildChanged", "found changed index")
                        val idx = list.indexOf(item)
                        Log.d("RLVM_onChildChanged-idx", idx.toString())
                        if (changed != null) {
                            list[idx] = changed
                            Log.d("RLVM_onChildChanged", "item exchanged")
                        }
                    }
                }
                reservationList.postValue(list)
            }catch(e:Exception){
                Log.e("ReservationListVM", "onChildChanged " + (e.message?:"null"))
                e.printStackTrace()
            }
        }

        override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
            val tag = "RLVM_onChildAdded"
            Log.d(tag, "onChildAdded-prevID: " + (previousChildName?:"null"))
            Log.d(tag, "onChildAdded-ID: " + (dataSnapshot.key?:"null"))
            val reservation: ReservationItem? = dataSnapshot.getValue<ReservationItem>()
            if (reservation != null) {
                val list = reservationList.value!!
                Log.d(tag, "onChildAdded-name: " + (reservation.userName?:"null"))
                reservation.addedTime = dataSnapshot.key
                reservation.adaptTime()
                if((selectedDay.after(reservation.calStartTime)&&selectedDay.before(reservation.calEndTime)) || isSameDay(selectedDay, reservation.calStartTime) || isSameDay(selectedDay, reservation.calEndTime)) {
                    list.add(reservation)
                    Log.d(tag, "child-Added")
                    reservationList.postValue(list)
                }
            }
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            val reservation: ReservationItem? = dataSnapshot.getValue<ReservationItem>()
            val addedTime = dataSnapshot.key
            Log.d("ReservationListVM", "onChildRemoved-Key $addedTime")
            Log.d("ReservationListVM", "onChildRemoved-Name " + (reservation?.userName?:"null"))
            reservation?.addedTime = addedTime
            reservation?.adaptTime()
            val list = reservationList.value!!
            val pos = list.indexOf(reservation)

            Log.d("ReservationListVM", "onChildRemoved-idx $pos")
            if(pos>=0) {
                list.removeAt(pos)
            }
        }
    }

    private var isPassed: Boolean = false
    private var currentDay: Calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.KOREA)
    private var selectedDay:Calendar = currentDay

    init{
        reservationList.value = ArrayList()
        ref = database.getReference("Reservation/$cardID")
        query = ref.orderByChild("startTime")
    }
    fun startQuery(){
        isQueryAvailable = try{
            query.addChildEventListener(valueEventListener)
            Log.d("RLVM", "startQuery executed")
            true
        }catch (e:Exception){
            Log.e("ReservationListVM", "startQuery->"+e.message)
            e.printStackTrace()
            pushToast("데이터베이스 연결에 실패했습니다.")
            false
        }
    }
    fun stopQuery(){
        if(isQueryAvailable) query.removeEventListener(valueEventListener)
        reservationList.value = ArrayList()
    }
    private fun setQuery(isPassed:Boolean, startDay:String, endDay:String){
        try {
            ref = if (isPassed) database.getReference("Reservation/$cardID")
            else database.getReference("Reservation/$cardID")
            query = ref.orderByChild("startTime")
                    .startAt(startDay, "startTime")
                    .endAt(endDay, "startTime")
        }catch(e:Exception){
            Log.e("RLVM", "setQuery->"+e.message)
            e.printStackTrace()
            pushToast("데이터베이스 설정 실패")
        }
        Log.d("RLVM-setQuery", startDay)
        Log.d("RLVM-setQuery", endDay)
        this.isPassed = isPassed
    }
    fun reload() {
        stopQuery()
        startQuery()
    }
    fun reload(day:CalendarDay){
        var calday = CalendarDay.from(currentDay.get(Calendar.YEAR), currentDay.get(Calendar.MONTH)+1, currentDay.get(Calendar.DAY_OF_MONTH))
        var ispassed = day.isBefore(calday)
        selectedDay.set(day.year, day.month-1, day.day,0,0,0)
        Log.d("RLVM_reload", "selectedDay.time:${selectedDay.time.time}")
        Log.d("RLVM_reload", "selectedDay.time:${selectedDay.time}")
        var queryStart = calendarDayToString(day, -3)
        var queryEnd = calendarDayToString(day, 3)
        setQuery(ispassed, queryStart, queryEnd)
        reload()
    }
    fun getList():ArrayList<ReservationItem>{
        return reservationList.value!!
    }

    fun removeItem(position: Int) {
        TODO("Authorization will be needed")
        if (position < reservationList.value?.size ?: 0) reservationList.value?.get(position)?.addedTime?.let { ref.child(it.toString()).removeValue() }
        else {
            Log.e("removeItem", "position index overflows list")
        }
    }
    private fun isSameDay(a:Calendar, b:Calendar):Boolean{
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
                a.get(Calendar.MONTH) == b.get(Calendar.MONTH) &&
                a.get(Calendar.DAY_OF_MONTH) == b.get(Calendar.DAY_OF_MONTH)
    }

    private fun calendarDayToString(calDay:CalendarDay, day:Int=0):String{
        var da = Calendar.getInstance()
        da.set(calDay.year, calDay.month-1, calDay.day)
        da.add(Calendar.DAY_OF_MONTH, day)
        var queryString = "${da.get(Calendar.YEAR)}."
        var num = da.get(Calendar.MONTH)+1
        queryString += if(num<10) "0${num}."
        else "${num}."
        num = da.get(Calendar.DAY_OF_MONTH)
        queryString += if(num<10) "0${num}"
        else "$num"
        return queryString
    }

    var toastMessage : SingleLiveEvent<String> = SingleLiveEvent()

    fun pushToast(message:String){
        toastMessage.postValue(message)
    }
}