package com.hongbaek.cardreservation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import splitties.activities.start

class MainActivity : AppCompatActivity() {
    val cardID = "01"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        start<ReservationListActivity> {
            this.putExtra("cardID", cardID)
        }
        findViewById<Button>(R.id.button).setOnClickListener {
            start<ReservationListActivity> {
            this.putExtra("cardID", cardID)
        }}

    }
}