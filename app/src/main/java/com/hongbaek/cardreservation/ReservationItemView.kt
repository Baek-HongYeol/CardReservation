package com.hongbaek.cardreservation

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout

class ReservationItemView(context: Context) : LinearLayout(context) {
    init{
        LayoutInflater.from(context)
            .inflate(R.layout.listreserveitem_view, this, true)
    }

}