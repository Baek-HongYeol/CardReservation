package com.hongbaek.cardreservation

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.functions.FirebaseFunctionsException
import com.hongbaek.cardreservation.utils.SundayDecorator
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.*
import splitties.activities.start
import splitties.alertdialog.*
import splitties.alertdialog.alertDialog
import splitties.alertdialog.appcompat.*
import splitties.alertdialog.material.materialAlertDialog
import splitties.toast.toast

class CalendarActivity : AppCompatActivity() {
    private val TAG = "Calendar_A"
    private val viewModel: ReservationListViewModel  by lazy{
        ViewModelProvider(this, ReservationListViewModel.Factory(objectID)).get(ReservationListViewModel::class.java)
    }
    private val objectID:String by lazy{
        if(intent.getStringExtra("objectId")==null)
            "Card"
        else
            intent.getStringExtra("objectID").toString()
    }
    private lateinit var calendarView: MaterialCalendarView
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerViewManager: RecyclerView.LayoutManager
    private lateinit var slidingUPL: SlidingUpPanelLayout
    private lateinit var fab_add: FloatingActionButton
    private var fabClickListener: View.OnClickListener = View.OnClickListener {
        Log.d("fabClickListener", "'executed")
        val day = calendarView.selectedDate!!
        start<ScheduleCreateActivity>{
            putExtra("sYear", day.year)
            putExtra("sMonth", day.month)
            putExtra("sDate", day.day)
        }
    }

    private val recyclerViewAdaptor: ReserveAdaptor by lazy{
        val adaptor = ReserveAdaptor(object : ReserveAdaptor.ItemEventListener {
            override fun onClick(v: View, position: Int) {
                alertDialog {
                    title = viewModel.getList()[position].title
                    message = viewModel.getItemDetail(position)
                    okButton()
                }.show()
            }

            override fun onLongClick(v: View, position: Int): Boolean {
                materialAlertDialog {
                    title = viewModel.getList()[position].title
                    message = viewModel.getItemDetail(position)
                    setPositiveButton(R.string.returning) { _, _ ->
                        val item = viewModel.getList()[position]
                        materialAlertDialog {
                            title = "예약 이름: " + item.title
                            val view = layoutInflater.inflate(R.layout.dialog_return, null)
                            setView(view)
                            setPositiveButton(R.string.returning) { _, _ ->
                                val progressBarActivity = ProgressBarActivity(this@CalendarActivity)
                                progressBarActivity.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                progressBarActivity.show()
                                progressBarActivity.setMessage(R.string.checkPassword)

                                viewModel.completeSchedule(position, view.findViewById<TextView>(R.id.passwordIET).text.toString())
                                        .addOnCompleteListener { task ->
                                            Log.d(TAG, "completeSchedule onComplete - task: ${task.toString()}")
                                            if (!task.isSuccessful) {
                                                val e = task.exception
                                                if (e is FirebaseFunctionsException) {
                                                    val code = e.code
                                                    val details = e.details
                                                    Log.e(TAG, "completeSchedule - error code: $code")
                                                    Log.e(TAG, "completeSchedule - error detail: $details")
                                                    alertDialog {
                                                        title = "Error"
                                                        if (BuildConfig.DEBUG) message = "오류가 발생했습니다.(DEBUG)\n${e.details}"
                                                        else message = "오류가 발생했습니다."
                                                    }.show()
                                                } else {
                                                    Log.e(TAG, "completeSchedule - error: $e")
                                                    alertDialog {
                                                        title = "Error"
                                                        if (BuildConfig.DEBUG) message = "오류가 발생했습니다.(DEBUG)\n${e?.message}"
                                                        else message = "오류가 발생했습니다."
                                                    }.show()
                                                }
                                                progressBarActivity.dismiss()
                                            } else {
                                                val result = task.result
                                                if (result?.containsKey("msg1") == true) {
                                                    Log.d(
                                                            TAG,
                                                            "completeSchedule_result - $result"
                                                    )
                                                    alertDialog {
                                                        title = result["msg1"].toString()
                                                        message = result["msg2"].toString()
                                                        Log.d(TAG, "status update complete alertDialog show")
                                                        okButton()
                                                    }.show()
                                                    progressBarActivity.dismiss()
                                                } else {
                                                    Log.d(
                                                            TAG,
                                                            "completeSchedule_result - $result"
                                                    )
                                                    alertDialog {
                                                        title = "Error"
                                                        message = "오류가 발생했습니다.\n 새로고침하여 결과를 확인하세요."
                                                    }
                                                    progressBarActivity.dismiss()
                                                }
                                            }
                                            viewModel.reload()
                                        }
                            }
                        }.onShow() {
                            if (BuildConfig.VERSION_CODE >= 23) {
                                positiveButton.setTextColor(context.getColor(R.color.colorOnPrimary))
                            } else {
                                positiveButton.setTextColor(resources.getColor(R.color.colorOnPrimary))
                            }
                        }.show()

                    }
                    setNegativeButton(R.string.delete) { _, _ ->
                        val item = viewModel.getList()[position]
                        alertDialog {
                            title = "예약 이름: " + item.title
                            val view = layoutInflater.inflate(R.layout.dialog_delete, null)
                            setView(view)
                            setPositiveButton(R.string.delete) { _, _ ->
                                val progressBarActivity = ProgressBarActivity(this@CalendarActivity)
                                progressBarActivity.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                progressBarActivity.show()
                                progressBarActivity.setMessage(R.string.checkPassword)

                                viewModel.deleteSchedule(position, view.findViewById<TextView>(R.id.passwordET).text.toString())
                                        .addOnCompleteListener { task ->
                                            if (!task.isSuccessful) {
                                                val e = task.exception
                                                if (e is FirebaseFunctionsException) {
                                                    val code = e.code
                                                    val details = e.details
                                                    Log.e(TAG, "deleteSchedule - error code: $code")
                                                    Log.e(TAG, "deleteSchedule - error detail: $details")
                                                    alertDialog {
                                                        title = "Error"
                                                        if (BuildConfig.DEBUG) message = "오류가 발생했습니다.(DEBUG)\n${e.details}"
                                                        else message = "오류가 발생했습니다."
                                                    }.show()
                                                } else {
                                                    Log.e(TAG, "deleteSchedule - error: $e")
                                                    alertDialog {
                                                        title = "Error"
                                                        if (BuildConfig.DEBUG) message = "오류가 발생했습니다.(DEBUG)\n${e?.message}"
                                                        else message = "오류가 발생했습니다."
                                                    }.show()
                                                }
                                                progressBarActivity.dismiss()
                                            } else {
                                                val result = task.result
                                                if (result?.containsKey("msg1") == true) {
                                                    Log.d(
                                                            TAG,
                                                            "deleteSchedule_result - ${result.get("msg1") ?: "null"}"
                                                    )
                                                    alertDialog {
                                                        title = result["msg1"].toString()
                                                        message = result["msg2"].toString()
                                                        Log.d(
                                                                TAG,
                                                                "deletion complete alertDialog show"
                                                        )
                                                        okButton()
                                                    }.show()
                                                    progressBarActivity.dismiss()
                                                } else {
                                                    Log.d(
                                                            TAG,
                                                            "deleteSchedule_result - ${result?.get("msg1") ?: "null"}"
                                                    )
                                                    alertDialog {
                                                        title = "Error"
                                                        message = "오류가 발생했습니다.\n 새로고침하여 결과를 확인하세요."
                                                    }
                                                    progressBarActivity.dismiss()
                                                }
                                            }
                                            viewModel.reload()
                                        }

                            }
                            cancelButton()
                        }.show()

                    }

                }.onShow {
                    if (BuildConfig.VERSION_CODE >= 23) {
                        positiveButton.setTextColor(context.getColor(R.color.colorOnPrimary))
                        negativeButton.setTextColor(context.getColor(R.color.colorOnPrimary))
                    } else {
                        positiveButton.setTextColor(resources.getColor(R.color.colorOnPrimary))
                        negativeButton.setTextColor(resources.getColor(R.color.colorOnPrimary))
                    }
                }.show()
                return true
            }
        })
        adaptor.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                Log.d("$TAG/SUP_L", "onItemRangeInserted: executed")
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                Log.d("$TAG/SUP_L", "onItemRangeRemoved: executed")
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
        calendarView.setTitleFormatter { day ->
            "${day.year} ${day.month}월"
        }
        val sundayDecorator = SundayDecorator(calendarView)
        calendarView.addDecorators(sundayDecorator)
        sundayDecorator.onMonthChanged(CalendarDay.today().month)

        viewModel.initQuery()

        recyclerViewManager = LinearLayoutManager(this)
        viewModel.reservationList.observe(this, Observer { reservationList: List<ReservationItem>? ->
            Log.d(TAG, "list change observed")
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

        calendarView.setOnDateChangedListener { _, date, selected ->
            Log.d(TAG, "calendarView_dateChanged")
            if(selected) viewModel.reload(date)
            if(slidingUPL.panelState == PanelState.EXPANDED) slidingUPL.panelState = PanelState.COLLAPSED
        }
        calendarView.setOnMonthChangedListener { _, date ->
            sundayDecorator.onMonthChanged(date.month)
            calendarView.invalidateDecorators()
        }

        val header =calendarView.findViewById<LinearLayout>(R.id.header)
        header.setBackgroundColor(ContextCompat.getColor(header.context, R.color.colorPrimary))
        header.findViewById<TextView>(R.id.month_name).setBackgroundColor(ContextCompat.getColor(header.context, R.color.colorPrimary))

        recyclerView.addItemDecoration(RecyclerViewDecoration(30))
        slidingUPL.addPanelSlideListener(object : PanelSlideListener {
            override fun onPanelSlide(panel: View?, slideOffset: Float) {}

            override fun onPanelStateChanged(
                    panel: View?,
                    previousState: PanelState?,
                    newState: PanelState?
            ) {
                Log.d("$TAG/SUP_L", "onPanelStateChange: previousState: $previousState")
                Log.d("$TAG/SUP_L", "onPanelStateChange: newState: $newState")
                if (newState == PanelState.EXPANDED) {
                    fab_add.show()
                } else if (newState == PanelState.DRAGGING) {
                    fab_add.hide()
                }
            }
        })
        slidingUPL.panelState = PanelState.COLLAPSED

        val shadowView = findViewById<View>(R.id.calendarCL)
        shadowView.isClickable = true
        slidingUPL.setFadeOnClickListener { slidingUPL.panelState = PanelState.COLLAPSED }

        fab_add.hide()
        fab_add.setOnClickListener( fabClickListener )

        viewModel.toastMessage.observe(this) { res ->
            if (res != null) {
                val message = res.toString()
                toast(message)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startQuery()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopQuery()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_calendar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.setting -> {
                start<SettingActivity>()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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