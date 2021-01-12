package com.hongbaek.cardreservation

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hongbaek.cardreservation.utils.ReservationDiffUtilCallback
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Exception


class ReserveHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

    @SuppressLint("SetTextI18n")
    fun bind(item:ReservationItem){
        Log.d("RHolder_bind-name", item.userName?:"null")
        itemView.findViewById<TextView>(R.id.userName).text = item.userName
        itemView.findViewById<TextView>(R.id.periodTextView).text = item.startTime + " ~ " + item.endTime
    }

}
class ReserveAdaptor(private val itemEventListener:ItemEventListener): RecyclerView.Adapter<ReserveHolder>() {
    private var list: MutableList<ReservationItem> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReserveHolder {
        val view = ReservationItemView(parent.context)
        val lp = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        view.layoutParams = lp
        Log.d("RLA_onCreateViewHolder", "executed")
        return ReserveHolder(view)
    }

    override fun getItemCount(): Int {
        Log.d("getItemCount", "size:" + list.size.toString())
        return list.size
    }

    override fun onBindViewHolder(holder: ReserveHolder, position: Int) {
        Log.d("RA_onBindViewHolderpos", position.toString())
        Log.d("RA_observe_list", list.size.toString())
        Log.d("RA_observe_list_Name", list[0].userName?:"null")
        holder.bind(list[position])
        holder.itemView.setOnClickListener {
            itemEventListener.onClick(it, position)
        }
        holder.itemView.setOnLongClickListener {
            itemEventListener.onLongClick(it, position)
        }
    }
    fun updateAdaptor(updatedList : List<ReservationItem>){
        val result = DiffUtil.calculateDiff(ReservationDiffUtilCallback(list, updatedList))
        list = updatedList.toMutableList()
        result.dispatchUpdatesTo(this)
    }

    interface ItemEventListener : EventListener {
        fun onClick(v: View, position: Int)
        fun onLongClick(v: View, position: Int): Boolean
    }
}