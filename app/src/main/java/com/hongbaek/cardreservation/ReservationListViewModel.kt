package com.hongbaek.cardreservation

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue

class ReservationListViewModel(cardID:String) : ViewModel() {
    class Factory(private val carID:String) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            Log.d("RLVM Factory-carID", "$carID")
            return ReservationListViewModel(carID) as T
        }
    }
    var reservationList: MutableLiveData<ArrayList<ReservationItem>> = MutableLiveData()

    private val database = FirebaseDatabase.getInstance()
    private val ref by lazy{
        database.getReference("Reservation/$cardID")
    }
    private val query: Query by lazy{ref.orderByChild("startTime")}
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
                    if(item.userName.equals(changed?.userName)){
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
            Log.d("ReservationListVM", "onChildAdded-prevID: " + (previousChildName?:"null"))
            val reservation: ReservationItem? = dataSnapshot.getValue<ReservationItem>()
            Log.d("ReservationListVM", "onChildAdded-ID: " + (dataSnapshot.key?:"null"))
            val list = reservationList.value!!
            if (reservation != null) {
                Log.d("ReservationListVM", "onChildAdded-name" + (reservation.userName?:"null"))
                reservation.addedTime = dataSnapshot.key
                reservation.timeConvertToCalender()
                list.add(reservation)
                reservationList.postValue(list)
            }
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            val reservation: ReservationItem? = dataSnapshot.getValue<ReservationItem>()
            val addedTime = dataSnapshot.key
            Log.d("ReservationListVM", "onChildRemoved-Key " + (addedTime?:"null"))
            Log.d("ReservationListVM", "onChildRemoved-Name " + (reservation?.userName?:"null"))
            val pos = addedTime?.toInt()?.minus(1)//list.indexOf(reservation)

            val list = reservationList.value!!
            Log.d("ReservationListVM", "onChildRemoved-idx " + pos.toString())
            pos?.let {
                list.removeAt(it)
            }
        }
    }

    init{
        reservationList.value = ArrayList()
    }
    fun startQuery(){
        isQueryAvailable = try{
            query.addChildEventListener(valueEventListener)
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
    fun reload() {
        stopQuery()
        startQuery()
    }
    fun getList():ArrayList<ReservationItem>{
        return reservationList.value!!
    }

    fun removeItem(position: Int) {
        TODO("Authorization will be needed")
        if (position < reservationList.value?.size ?: 0) reservationList.value?.get(position)?.addedTime?.let { ref.child(it).removeValue() }
        else {
            Log.e("removeItem", "position index overflows list")
        }
    }

    var toastMessage : SingleLiveEvent<String> = SingleLiveEvent()

    fun pushToast(message:String){
        toastMessage.postValue(message)
    }
}