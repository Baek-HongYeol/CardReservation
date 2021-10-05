package com.hongbaek.cardreservation

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import splitties.toast.toast

class SettingViewModel: ViewModel() {
    private val TAG = "SettingVM"
    private val database: FirebaseDatabase by lazy{ Firebase.database }
    private var ref: DatabaseReference = database.getReference("Reservation/Auth")

    private var auth: FirebaseAuth = Firebase.auth

    fun login( data: HashMap<String, *> ): Task<HashMap<String, Any>> {
        var msg1=""
        var msg2=""
        var code=0
        var myCF = MyCloudFuction()
        return myCF.login(data).continueWith { task ->
            var result = task.result as HashMap<String, Any>
            code = result["code"] as Int
            if (result.containsKey("msg1") && result.containsKey("msg2")) {
                msg1 = result["msg1"].toString()
                msg2 = result["msg2"].toString()
            } else {
                try {
                    msg1 = "로그인 실패"
                    msg2 += "\n CF_Code:${result["code"]}"
                } catch (e: Exception) {
                    msg1 = "Error"
                    msg2 = "로그인 실패\n 결과 수신 중 에러가 발생하였습니다.\n${e.message}"
                    Log.e(TAG, "getHttpsCallable.call: $e")
                }
            }

            if(code==1)
                startSignIn(result["token"].toString())

            msg2 += "\n CF_Code:${result["code"]}"
            hashMapOf("msg1" to msg1, "msg2" to msg2, "code" to code)
        }
    }

    private fun startSignIn(customToken: String?) {
        customToken?.let {
            auth.signInWithCustomToken(it)
                    .addOnCompleteListener { task ->
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
        toast("login & token received by ${user?.displayName}")
    }


}