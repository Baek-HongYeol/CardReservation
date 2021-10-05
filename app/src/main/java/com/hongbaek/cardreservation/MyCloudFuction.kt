package com.hongbaek.cardreservation

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.HttpsCallableResult
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import java.lang.ClassCastException
import kotlin.collections.HashMap

class MyCloudFuction {

    private val TAG = "MyCloudFuction"
    private val functions: FirebaseFunctions by lazy{ Firebase.functions }

    private fun functionCall(name:String, data:Map<String, Any>): Task<HttpsCallableResult>{
        return functions.getHttpsCallable(name).call(data)
    }


    fun deleteSchedule(data:HashMap<String, *>): Task<Map<String, Any>>{
        Log.d(TAG, "deleteSchedule: sending data: $data")
        return functionCall("deleteSchedule", data)
                .continueWith { task ->
                    var msg1 = ""
                    var msg2 = ""
                    var code = 0
                    var error = ""

                    if(!task.isSuccessful){
                        var e = task.exception
                        if (e is FirebaseFunctionsException) {
                            val ecode = e.code
                            val details = e.details
                            Log.e(TAG, "deleteSchedule - error code: $ecode")
                            Log.e(TAG, "deleteSchedule - error detail: $details")
                            msg1 = "Error"
                            code = 13
                            error = e.details.toString() + "e_code: $ecode"

                        } else {
                            Log.e(TAG, "deleteSchedule - error: $e")
                            msg1 = "Error"
                            code = 14
                            error = e?.message.toString()
                        }
                    }
                    else{
                        try {
                            var result = task.result as HttpsCallableResult
                            var map: Map<*, *>
                            try {
                                map = result.data as Map<*, *>
                                if (map.containsKey("message")) {           // 삭제 성공
                                    code = 1
                                    Log.d(TAG, "Deletion: Succeed")
                                } else {                                    // 실패
                                    try {
                                        code = if (task.isComplete) 2 else 3
                                        error = map["error"].toString()

                                        Log.d(TAG, "delete: HttpsCallable Result has other data: $map")
                                        Log.w(TAG, "delete: Task throw Exception: ${task.exception}")
                                    } catch (e: Exception) {
                                        code = 10
                                        error += e.message?:"null"
                                        Log.e(TAG, "getHttpsCallable.call: $e")
                                        if(BuildConfig.DEBUG)
                                            error += "\n =======DEBUG=======\n${task.exception}"
                                    }
                                }
                            } catch (e: ClassCastException) {
                                code = 11
                                error += e.message?:"null"
                                Log.e(TAG, "getHttpsCallable.call: $e")
                                if(BuildConfig.DEBUG)
                                    error += "\n =======DEBUG=======\n${task.exception}"
                            }
                        } catch (e: Exception) {
                            code = 12
                            error += e.message?:"null"
                            Log.e(TAG, "getHttpsCallable.call: $e")
                            if(BuildConfig.DEBUG)
                                error += "\n =======DEBUG=======\n${task.exception}"

                        }
                    }
                    hashMapOf("msg1" to msg1, "msg2" to msg2, "code" to code, "error" to error)

                }
    }

    /** login - password를 전달해 서버에서 인증과 토큰 발급 후 토큰을 받아오는 함수
     * @return HashMap("msg1", "msg2", "code",
     * if   token exists, return code 1, with token.
     * else, return a code with error message to msg2.
     * return ;   */
    fun login(data: HashMap<String, *>): Task<Map<String, Any>> { // 로그인으로 토큰 받아오기
        Log.d(TAG, "login: sending data: $data")
        return functionCall("login", data)
                .continueWith { task ->
                    var msg1 = ""
                    var msg2 = ""
                    var code = 0
                    var token = ""
                    try {
                        var result = task.result as HttpsCallableResult
                        var map: Map<*, *>
                        try {
                            map = result.data as Map<*, *>
                            if (map.containsKey("token")) {
                                code = 1
                                msg1 = "로그인 성공"
                                msg2 = "인증 되었습니다."
                                token = map["token"].toString()
                                Log.d(TAG, "login: Succeed")
                            } else {
                                try {
                                    msg1 = "로그인 실패"
                                    if (task.isComplete) {
                                        msg2 = "권한이 없습니다."
                                        code = 2
                                    }
                                    else {
                                        msg2 = "에러가 발생하였습니다."
                                        code = 3
                                    }
                                    msg2 += "\n"
                                    if(BuildConfig.DEBUG) {
                                        msg2 += map["error"]
                                        msg2 += "\n=====Debug======\nexception: ${task.exception}"
                                    }
                                    Log.d(TAG, "login: HttpsCallable Result has other data: $map")
                                    Log.w(TAG, "login: Task throw Exception: ${task.exception}")
                                } catch (e: Exception) {
                                    msg1 = "Error"
                                    code = 10
                                    msg2 = "로그인 실패\n 결과 수신 중 에러가 발생하였습니다.(code: $code)\n${e.message}"
                                    hashMapOf("msg1" to msg1, "msg2" to msg2, "code" to code)
                                    Log.e(TAG, "getHttpsCallable.call(code:$code) : $e")
                                }
                            }
                        } catch (e: ClassCastException) {
                            msg1 = "Error"
                            code = 11
                            msg2 = "로그인 실패\n 결과 수신 중 에러가 발생하였습니다.(code: $code)\n${e.message}"
                            hashMapOf("msg1" to msg1, "msg2" to msg2, "code" to code)
                            Log.e(TAG, "getHttpsCallable.call(code:$code) : $e")
                        }
                    } catch (e: Exception) {
                        msg1 = "Error"
                        code = 12
                        msg2 = "인증 중 오류가 발생했습니다. 잠시 후 다시 시도하거나 문의해주세요.(code: $code)"
                        hashMapOf("msg1" to msg1, "msg2" to msg2, "code" to code)
                        Log.e(TAG, "getHttpsCallable.call(code:$code) : $e")
                    }
                    var messages = hashMapOf("msg1" to msg1, "msg2" to msg2, "code" to code)
                    if(code==1){
                        messages["token"] = token
                        if(token==null) {
                            messages["code"] = 0
                            messages["msg2"] = messages["msg2"].toString()+ "\nBut token is not issued"
                        }
                        if(BuildConfig.DEBUG)
                            Log.d(TAG, "token is - $token")
                    }
                    messages

                }
    }
}