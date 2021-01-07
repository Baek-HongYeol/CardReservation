package com.hongbaek.cardreservation

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout

class ReservationItemView(context: Context) : LinearLayout(context) {
    init{
        LayoutInflater.from(context)
            .inflate(R.layout.listreserveitem_view, this, true)
    }

}