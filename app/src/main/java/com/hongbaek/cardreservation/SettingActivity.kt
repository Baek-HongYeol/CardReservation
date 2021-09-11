package com.hongbaek.cardreservation

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import splitties.alertdialog.appcompat.message
import splitties.alertdialog.material.materialAlertDialog
import splitties.toast.toast

class SettingActivity : AppCompatActivity() {
    private val TAG = "SettingActivity"
    private val viewModel: SettingViewModel by viewModels()

    private lateinit var toolbar: Toolbar
    private lateinit var imageInfo: ImageButton
    private lateinit var infoTV: TextView
    private lateinit var switch: SwitchMaterial
    private lateinit var passwordIET: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        imageInfo = findViewById(R.id.imageButton)
        infoTV = findViewById(R.id.textView3)
        switch = findViewById(R.id.switch1)
        passwordIET = findViewById(R.id.accPasswordIET)

        imageInfo.setOnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN -> infoTV.visibility = View.VISIBLE
                MotionEvent.ACTION_UP -> {
                    infoTV.visibility = View.GONE
                    v.performClick()
                }
                else -> true
            }
            true
        }

        switch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                if(passwordIET.text.isNullOrBlank())
                    toast("관리자 암호를 입력하셔야 활성화 됩니다.")
                else{
                    login(passwordIET.text.toString())
                }
            }
            else{
                //updatePermission(null)
            }
        }

    }

    private fun login(password:String){
        val progressBarActivity = ProgressBarActivity(this)
        progressBarActivity.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        progressBarActivity.show()
        progressBarActivity.setMessage(R.string.checkPassword)
        var data = hashMapOf(
                "password" to password,
                "admin" to switch.isChecked
        )

        var msg1=""
        var msg2=""
        var code=0

        viewModel.login(data).continueWith { task ->
            var result = task.result!!
            msg1 = result["msg1"].toString()
            msg2 = result["msg2"].toString()
            code = result["code"] as Int
            if (code == 1) {
                progressBarActivity.dismiss()
                switch.isChecked = true
            } else {
                progressBarActivity.dismiss()
                materialAlertDialog {
                    title = msg1
                    message = msg2
                    val view = layoutInflater.inflate(R.layout.dialog_return, null)
                    setView(view)
                }
                switch.isChecked = false
            }
        }


    }

}