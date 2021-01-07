package com.hongbaek.cardreservation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hongbaek.cardreservation.utils.CalendarDayDiffUtillCallBack

class CalendarAdaptor:RecyclerView.Adapter<CalendarViewHolder>() {
    private var list: MutableList<CalendarDay> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.calender_cell_view, LinearLayout(parent.context), true)
        val lp = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        view.layoutParams = lp
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateAdaptor(updatedList: List<CalendarDay>){
        var result = DiffUtil.calculateDiff(CalendarDayDiffUtillCallBack(list, updatedList))
        list = updatedList.toMutableList()
        result.dispatchUpdatesTo(this)
    }

}

class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    fun bind(item: CalendarDay){

        itemView.setOnClickListener {

        }
        itemView.setOnLongClickListener {
            false
        }
    }
}

class CalendarDay{
    var day:String = "0"
    var month:Int = 0
    var year: Int = 1970

    fun isSameDay(d: String): Boolean{
        return day.equals(d)
    }

    fun isSameMonth(m: Int): Boolean{
        return month == m
    }
    fun isSameYear(y: Int): Boolean{
        return year == y
    }

    fun isSame(instance: CalendarDay): Boolean{
        return isSameDay(instance.day) && isSameMonth(instance.month) && isSameYear(instance.year)
    }

}