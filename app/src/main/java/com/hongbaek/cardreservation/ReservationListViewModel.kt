package com.hongbaek.cardreservation

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.lang.ClassCastException
import java.util.*
import kotlin.collections.ArrayList


class ReservationListViewModel(val cardID:String) : ViewModel() {
    class Factory(private val _cardID:String) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            Log.d("RLVM Factory-cardID", _cardID)
            return ReservationListViewModel(_cardID) as T
        }
    }
    private val TAG = "ReservationListVM"
    var reservationList: MutableLiveData<ArrayList<ReservationItem>> = MutableLiveData()
    private val functions:FirebaseFunctions by lazy{ Firebase.functions }
    private val database:FirebaseDatabase by lazy{ Firebase.database }
    private var ref:DatabaseReference = database.getReference("Reservation/$cardID")
    private lateinit var query: Query
    private var startQuery: String = ""
    private var endQuery: String = ""
    private var isQueryAvailable:Boolean = false
    private val valueEventListener = object : ChildEventListener {
        private val mTAG = "RealTimeDB/Reservation/$cardID"
        override fun onCancelled(error: DatabaseError) {
            // Failed to read value
            Log.w(mTAG, "Failed to read value."+error.message, error.toException())
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
                Log.d(tag, "onChildAdded-Title " + (reservation.title?:"null"))
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
            Log.d("ReservationListVM", "onChildRemoved-Title " + (reservation?.title?:"null"))
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
    private var selectedDay:Calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.KOREA)

    init{
        reservationList.value = ArrayList()
    }
    fun initQuery(){
        var calday = CalendarDay.today()
        Log.d("RLVM_initQuery", "today: $calday")
        var startQuery = calendarDayToString(calday, -3)
        var endQuery = calendarDayToString(calday, 3)
        try {
            ref = database.getReference("Reservation/$cardID")
            this.startQuery = startQuery
            this.endQuery = endQuery
            query = ref.orderByChild("startTime")
                    .startAt(this.startQuery, "startTime")
                    .endAt(this.endQuery, "startTime")
        }catch(e:Exception){
            Log.e("RLVM", "setQuery->"+e.message)
            e.printStackTrace()
            pushToast("데이터베이스 설정 실패")

        }
        Log.d("RLVM-initQuery", startQuery)
        Log.d("RLVM-initQuery", endQuery)
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
            startQuery = startDay
            endQuery = endDay
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
        var currentDay = Calendar.getInstance(TimeZone.getDefault(), Locale.KOREA)
        var calday = CalendarDay.from(currentDay.get(Calendar.YEAR), currentDay.get(Calendar.MONTH)+1, currentDay.get(Calendar.DAY_OF_MONTH))
        var isPassed = day.isBefore(calday)
        selectedDay.set(day.year, day.month-1, day.day,0,0,0)
        Log.d("RLVM_reload", "selectedDay.time:${selectedDay.time.time}")
        Log.d("RLVM_reload", "selectedDay.time:${selectedDay.time}")
        var queryStart = calendarDayToString(day, -3)
        var queryEnd = calendarDayToString(day, 3)
        setQuery(isPassed, queryStart, queryEnd)
        reload()
    }
    fun reload(day:Calendar){
        var calday = CalendarDay.from(day.get(Calendar.YEAR), day.get(Calendar.MONTH)+1, day.get(Calendar.DAY_OF_MONTH))
        reload(calday)
    }

    fun deleteSchedule(position: Int, password:String): Task<Map<String, *>> { // db에 삭제 명령 보내기.
        val data = hashMapOf(
                "password" to password,
                "refKey" to getList()[position].addedTime
        )
        Log.d(TAG, "deleteSchedule: sending data: $data")
        return functions.getHttpsCallable("checkPassword").call(data)
                .continueWith { task ->
                    var result: Any? = null
                    var msg1 = ""
                    var msg2 = ""
                    try {
                        result = task.result as Map<*, *>
                        if (result.containsKey("message")) {
                            msg1 = "삭제 완료"
                            msg2 = result["message"].toString()
                            Log.d(TAG, "deleteSchedule: Map<> Result has data")
                        } else {
                            try {
                                msg1 = "삭제 실패"
                                if (task.isComplete)
                                    msg2 = "삭제가 거부되었습니다."
                                else
                                    msg2 = "에러가 발생하였습니다."
                                msg2 += "\n" + result["error"]
                                Log.d(TAG, "deleteSchedule: Map<> Result has data: ${result["error"]}")
                            } catch (e: Exception) {
                                msg2 = "삭제 실패\n 결과 수신 중 에러가 발생하였습니다.\n${e.message}"
                                Log.e(TAG, "getHttpsCallable.call: $e")
                            }
                        }
                    } catch (e: ClassCastException) {
                        try {
                            result = task.result as HttpsCallableResult
                            var data: Map<*, *>
                            try {
                                data = result.data as Map<*, *>
                                if (data.containsKey("message")) {
                                    msg1 = "삭제 완료"
                                    msg2 = data["message"].toString()
                                    Log.d(TAG, "deleteSchedule: HttpsCallable Result has data")
                                } else {
                                    try {
                                        msg1 = "삭제 실패"
                                        if (task.isComplete)
                                            msg2 = "삭제가 거부되었습니다."
                                        else
                                            msg2 = "에러가 발생하였습니다."
                                        msg2 += "\n" + data["error"]
                                        Log.d(TAG, "deleteSchedule: HttpsCallable Result has data: ${data["error"]}")
                                    } catch (e: Exception) {
                                        msg1 = "Error"
                                        msg2 = "삭제 실패\n 결과 수신 중 에러가 발생하였습니다.\n${e.message}"
                                        Log.e(TAG, "getHttpsCallable.call: $e")
                                    }
                                }

                            } catch (e: ClassCastException) {
                                msg1 = "Error"
                                msg2 = "삭제 실패\n 결과 수신 중 에러가 발생하였습니다.\n${e.message}"
                                Log.e(TAG, "getHttpsCallable.call: $e")
                            }
                        } catch (e: Exception) {
                            msg1 = "Error"
                            msg2 = "결과를 받아오지 못했습니다. 새로고침하여 결과를 확인하세요."
                            Log.e(TAG, "getHttpsCallable.call: $e")
                        }
                    }
                    if(msg1=="삭제 완료") data["refKey"]?.let { removeItem(position, it) }
                    hashMapOf("msg1" to msg1, "msg2" to msg2)

                }
    }

    fun completeSchedule(position: Int, password:String): Task<Map<String, *>> {
        val data = hashMapOf(
            "password" to password,
            "refKey" to getList()[position].addedTime
        )
        Log.d(TAG, "completeSchedule: sending data: $data")
        return functions.getHttpsCallable("completeSchedule").call(data)
            .continueWith { task ->
                var result: Any? = null
                var msg1 = ""
                var msg2 = ""
                try {
                    result = task.result as Map<*, *>
                    if (result.containsKey("message")) {
                        msg1 = "변경 완료"
                        msg2 = result["message"].toString()
                    } else {
                        try {
                            msg1 = "변경 실패"
                            if (task.isComplete)
                                msg2 = "변경이 거부되었습니다."
                            else
                                msg2 = "에러가 발생하였습니다."
                            msg2 += "\n" + result["error"]
                        } catch (e: Exception) {
                            msg2 = "변경 실패\n 결과 수신 중 에러가 발생하였습니다.\n${e.message}"
                            Log.e(TAG, "getHttpsCallable.call: $e")
                        }
                    }
                } catch (e: ClassCastException) {
                    try {
                        result = task.result as HttpsCallableResult
                        var data: Map<*, *>
                        try {
                            data = result.data as Map<*, *>
                            if (data.containsKey("message")) {
                                msg1 = "변경 완료"
                                msg2 = data["message"].toString()
                            } else {
                                try {
                                    msg1 = "변경 실패"
                                    if (task.isComplete)
                                        msg2 = "변경이 거부되었습니다."
                                    else
                                        msg2 = "에러가 발생하였습니다."
                                    msg2 += "\n" + data["error"]
                                } catch (e: Exception) {
                                    msg1 = "Error"
                                    msg2 = "변경 실패\n 결과 수신 중 에러가 발생하였습니다.\n${e.message}"
                                    Log.e(TAG, "getHttpsCallable.call: $e")
                                }
                            }

                        } catch (e: ClassCastException) {
                            msg1 = "Error"
                            msg2 = "변경 실패\n 결과 수신 중 에러가 발생하였습니다.\n${e.message}"
                            Log.e(TAG, "getHttpsCallable.call: $e")
                        }
                    } catch (e: Exception) {
                        msg1 = "Error"
                        msg2 = "결과를 받아오지 못했습니다. 새로고침하여 결과를 확인하세요."
                        Log.e(TAG, "getHttpsCallable.call: $e")
                    }
                }
                if(msg1=="변경 완료") data["refKey"]?.let { removeItem(position, it) }
                hashMapOf("msg1" to msg1, "msg2" to msg2)

            }
    }

    private fun removeItem(position: Int, key:String){
        if (position < reservationList.value?.size ?: 0) reservationList.value?.get(position)?.addedTime?.let { ref.child(it.toString()).removeValue() }
        else {
            Log.e("removeItem", "position index overflows list")
        }
    }

    fun getList():ArrayList<ReservationItem>{
        return reservationList.value!!
    }
    fun getItemDetail(position: Int): String{
        var item = reservationList.value?.get(position)
        return "등록자: " + (item?.userName ?: "NULL") +
                "\n시작 시간: " + (item?.startTime ?: "NULL") +
                "\n반납 시간: " + (item?.endTime ?: "NULL") +
                "\n예상 금액: " + (item?.estimated ?: "NULL") + "원"
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