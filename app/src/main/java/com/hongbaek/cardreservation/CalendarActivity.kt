package com.hongbaek.cardreservation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hongbaek.cardreservation.utils.SundayDecorator
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import splitties.toast.toast

class CalendarActivity : AppCompatActivity() {
    private val viewModel: ReservationListViewModel  by lazy{
        ViewModelProvider(this, ReservationListViewModel.Factory(cardID)).get(ReservationListViewModel::class.java)
    }
    private val cardID:String by lazy{
        intent.getStringExtra("cardID").toString()
    }
    private lateinit var calendarView: MaterialCalendarView
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewManager: RecyclerView.LayoutManager
    private val recyclerViewAdaptor: ReserveAdaptor by lazy{
        val adaptor = ReserveAdaptor(object : ReserveAdaptor.ItemEventListener{
            override fun onClick(v: View, position: Int) {

            }

            override fun onLongClick(v: View, position: Int): Boolean {
                return false
            }
        })
        adaptor.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                Log.d("RLA_onItemRangeInserted", "executed")
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                Log.d("RLA_onItemRangeRemoved", "executed")
            }
        })
        adaptor
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        calendarView = findViewById(R.id.calendarView)
        recyclerView = findViewById(R.id.reservationList_RV)
        calendarView.setDateSelected(CalendarDay.today(), true)
        val sundayDecorator = SundayDecorator(calendarView)
        //calendarView.addDecorators(sundayDecorator)
        recyclerViewManager = LinearLayoutManager(this)
        viewModel.reservationList.observe(this, Observer { reservationList: List<ReservationItem>? ->
            if (reservationList == null) {
                return@Observer
            }
            recyclerViewAdaptor.updateAdaptor(reservationList)
        })
        recyclerViewAdaptor.updateAdaptor(viewModel.getList())
        recyclerView.adapter = recyclerViewAdaptor
        recyclerView.layoutManager = recyclerViewManager
        
        calendarView.setOnDateChangedListener { widget, date, selected ->
            if(selected) viewModel.reload(date)
        }
        viewModel.toastMessage.observe(this, Observer { res ->
            if (res != null) {
                val message = res.toString()
                toast(message)
            }
        })
    }
}