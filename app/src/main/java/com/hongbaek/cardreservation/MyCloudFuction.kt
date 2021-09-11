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
                    try {
                        var result = task.result as HttpsCallableResult
                        var map: Map<*, *>
                        try {
                            map = result.data as Map<*, *>
                            if (map.containsKey("token")) {
                                code = 1
                                msg1 = "로그인 성공"
                                msg2 = "인증 되었습니다."
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
                                        msg2 += "\n/\n${task.exception}"
                                    }
                                    Log.d(TAG, "login: HttpsCallable Result has other data: $map")
                                    Log.w(TAG, "login: Task throw Exception: ${task.exception}")
                                } catch (e: Exception) {
                                    msg1 = "Error"
                                    msg2 = "로그인 실패\n 결과 수신 중 에러가 발생하였습니다.\n${e.message}"
                                    code = 10
                                    Log.e(TAG, "getHttpsCallable.call: $e")
                                }
                            }
                        } catch (e: ClassCastException) {
                            msg1 = "Error"
                            msg2 = "로그인 실패\n 결과 수신 중 에러가 발생하였습니다.\n${e.message}"
                            code = 11
                            Log.e(TAG, "getHttpsCallable.call: $e")
                        }
                    } catch (e: Exception) {
                        msg1 = "Error"
                        msg2 = "결과를 받아오지 못했습니다. 새로고침하여 결과를 확인하세요." // ????
                        code = 12
                        Log.e(TAG, "getHttpsCallable.call: $e")
                    }
                    var messages = hashMapOf("msg1" to msg1, "msg2" to msg2, "code" to code)
                    if(code==1){
                        messages.put("token", data["token"].toString())
                    }
                    messages

                }
    }
}