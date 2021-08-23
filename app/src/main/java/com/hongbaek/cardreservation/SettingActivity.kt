package com.hongbaek.cardreservation

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import splitties.toast.toast

class SettingActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var toolbar: Toolbar
    private lateinit var imageInfo: ImageButton
    private lateinit var infoTV: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        imageInfo = findViewById(R.id.imageButton)
        infoTV = findViewById(R.id.textView3)
        imageInfo.setOnTouchListener (View.OnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN -> infoTV.visibility = View.VISIBLE
                MotionEvent.ACTION_UP -> {
                    infoTV.visibility = View.GONE
                    v.performClick()
                }
                else -> true
            }
            true
        })
        auth = Firebase.auth

    }

    private fun login(password:String){

    }

    private fun startSignIn(customToken: String?) {
        customToken?.let {
            auth.signInWithCustomToken(it)
                .addOnCompleteListener (this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Login in Setting", "signInWithCustomToken:success")
                        val user = auth.currentUser
                        updatePermission(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Login in Setting", "signInWithCustomToken:failure", task.exception)
                        toast("Authentication failed.")
                        updatePermission(null)
                    }
                }
        }
    }

    private fun updatePermission(user: FirebaseUser?) {

    }

}