package com.hongbaek.cardreservation.utils

import androidx.recyclerview.widget.DiffUtil
import com.hongbaek.cardreservation.CalendarDay

class CalendarDayDiffUtillCallBack(private val oldList: List<CalendarDay>, private val updatedList: List<CalendarDay>): DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return updatedList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].isSame(updatedList[newItemPosition])
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].isSame(updatedList[newItemPosition])
    }
}