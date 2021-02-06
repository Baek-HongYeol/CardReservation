package com.hongbaek.cardreservation

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import androidx.annotation.StringRes

class ProgressBarActivity(context: Context): Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.progress_popup)
        setCancelable(false)
    }
    fun setMessage(message:String){
        findViewById<TextView>(R.id.progressTV).text = message
    }
    fun setMessage(@StringRes resId:Int){
        findViewById<TextView>(R.id.progressTV).text = context.getText(resId)
    }
}