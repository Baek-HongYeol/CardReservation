package com.hongbaek.cardreservation

import androidx.appcompat.app.AppCompatActivity
import android.graphics.Rect
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.hongbaek.cardreservation.utils.EventDecorator
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hongbaek.cardreservation.utils.SundayDecorator
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.*
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
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerViewManager: RecyclerView.LayoutManager
    private lateinit var slidingUPL: SlidingUpPanelLayout
    private lateinit var fab_add: FloatingActionButton
    private var clickListener: View.OnClickListener = View.OnClickListener {
        Log.d("clickListener", "'executed")
        //ScheduleCreateDialog(this)
    }

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
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        slidingUPL = findViewById(R.id.slidingUpPanel)
        fab_add = findViewById(R.id.fab_reservation_add)

        calendarView.setDateSelected(CalendarDay.today(), true)
        val sundayDecorator = SundayDecorator(calendarView)
        calendarView.addDecorators(sundayDecorator)

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
        recyclerView.setHasFixedSize(true)

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.reload()
            swipeRefreshLayout.isRefreshing=false
        }

        calendarView.setOnDateChangedListener { widget, date, selected ->
            if(selected) viewModel.reload(date)
            if(slidingUPL.panelState == PanelState.EXPANDED) slidingUPL.panelState = PanelState.COLLAPSED
        }
        val header =calendarView.findViewById<LinearLayout>(R.id.header)
        header.setBackgroundColor(resources.getColor(R.color.colorPrimary, theme))
        val param = header.layoutParams as ViewGroup.MarginLayoutParams
        param.setMargins(0, 0, 0, 10)
        param.width = LinearLayout.LayoutParams.MATCH_PARENT
        header.layoutParams = param
        recyclerView.addItemDecoration(RecyclerViewDecoration(30));
        slidingUPL.addPanelSlideListener(object: PanelSlideListener{
            override fun onPanelSlide(panel: View?, slideOffset: Float) {}

            override fun onPanelStateChanged(
                panel: View?,
                previousState: PanelState?,
                newState: PanelState?
            ) {
                Log.d("SUPL_onPanelStateChange", "previousState: $previousState")
                Log.d("SUPL_onPanelStateChange", "newState: $newState")
                if(newState == PanelState.EXPANDED){
                    fab_add = findViewById<FloatingActionButton>(R.id.fab_reservation_add)
                    fab_add.show()

                    //fab_add.setOnClickListener(clickListener)
                }
                else if(newState == PanelState.DRAGGING){
                    findViewById<FloatingActionButton>(R.id.fab_reservation_add).hide()
                }
            }
        })

        fab_add.hide()
        fab_add.setOnClickListener( clickListener )

        viewModel.toastMessage.observe(this, Observer { res ->
            if (res != null) {
                val message = res.toString()
                toast(message)
            }
        })
    }
    inner class RecyclerViewDecoration(private val divHeight: Int) : ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.top = divHeight
        }
    }
}