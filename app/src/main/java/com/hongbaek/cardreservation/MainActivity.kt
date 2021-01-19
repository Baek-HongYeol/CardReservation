package com.hongbaek.cardreservation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import splitties.activities.start

class MainActivity : AppCompatActivity() {
    val cardID = "Card"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        start<CalendarActivity> {
            this.putExtra("cardID", cardID)
        }
        findViewById<Button>(R.id.button).setOnClickListener {
            start<CalendarActivity> {
            this.putExtra("cardID", cardID)
        }}

    }
}