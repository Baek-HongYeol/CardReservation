package com.hongbaek.cardreservation.utils

import androidx.recyclerview.widget.DiffUtil
import com.hongbaek.cardreservation.ReservationItem

class ReservationDiffUtilCallback(private val oldList:List<ReservationItem>, private val updatedList:List<ReservationItem>): DiffUtil.Callback(){
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItemTime = oldList[oldItemPosition].addedTime
        val updatedItemTime = updatedList[newItemPosition].addedTime
        return oldItemTime.equals(updatedItemTime)
    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return updatedList.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = updatedList[newItemPosition]
        val isIdSame = oldItem.addedTime.equals(newItem.addedTime)
        val isNameSame = oldItem.userName.equals(newItem.userName)
        val isStartTimeSame = oldItem.startTime.compareTo(newItem.startTime) == 0
        val isEndTimeSame = oldItem.endTime.compareTo(newItem.endTime) == 0
        val isUIDSame = oldItem.uid.equals(newItem.uid)
        return isIdSame && isNameSame && isStartTimeSame && isEndTimeSame && isUIDSame
    }
}