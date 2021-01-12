package com.hongbaek.cardreservation

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CalendarView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import splitties.toast.longToast

class ReservationListActivity : AppCompatActivity() {
    private val viewModel : ReservationListViewModel by lazy{
        ViewModelProvider(this, ReservationListViewModel.Factory(cardID)).get(ReservationListViewModel::class.java)
    }
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdaptor: ReserveAdaptor
    private lateinit var viewManager : RecyclerView.LayoutManager


    private lateinit var mEmptyListMessage : TextView
    private lateinit var cardID: String
    private lateinit var list: ArrayList<ReservationItem>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservationlist)
        supportActionBar?.title = "Reservation"
        cardID = intent.getStringExtra("cardID").toString()
        /*viewModel.reservationList.observe(this, Observer<List<ReservationItem>> { reservationList: List<ReservationItem>? ->
            if (reservationList == null) {
                return@Observer
            }
            viewAdaptor.updateAdaptor(reservationList)
        })*/

        /* mEmptyListMessage = findViewById(R.id.emptymessageView)
        recyclerView = findViewById(R.id.RV_reservation)
        recyclerView.setHasFixedSize(true)
        viewManager = LinearLayoutManager(this)
        viewAdaptor = ReserveAdaptor(object : ReserveAdaptor.ItemEventListener {
                override fun onClick(v: View, position: Int) {
                    Log.d("RLA_onClickListener", "$position item clicked")
                    AlertDialog.Builder(this@ReservationListActivity)
                        .setMessage("")
                        .setNegativeButton(getString(R.string.ok), null)
                        .show()
                }

                override fun onLongClick(v: View, position: Int): Boolean {
                    AlertDialog.Builder(this@ReservationListActivity)
                        .setMessage("${list[position].userName} 항목을 삭제합니다.")
                        .setPositiveButton("Remove") { _, _ -> viewModel.removeItem(position) }
                        .setNegativeButton("Cancle", null)
                        .show()
                    return true
                }

            })
        recyclerView.layoutManager = viewManager
        viewAdaptor.updateAdaptor(viewModel.getList())
        viewAdaptor.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                Log.d("RLA_onItemRangeInserted", "executed")
                mEmptyListMessage.visibility = if (viewModel.getList().size == 0) View.VISIBLE else View.GONE
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                Log.d("RLA_onItemRangeRemoved", "executed")
                mEmptyListMessage.visibility = if (viewModel.getList().size == 0) View.VISIBLE else View.GONE
            }
        })
        recyclerView.adapter= viewAdaptor
        */

    }
    override fun onStart() {
        super.onStart()
        //viewModel.startQuery()
        val ns = NetworkStatus(this)
        if(!ns.checkNetworkState()){
            longToast("인터넷 연결이 필요합니다.")
        }
    }
    override fun onStop(){
        //viewModel.stopQuery()
        super.onStop()
    }

}