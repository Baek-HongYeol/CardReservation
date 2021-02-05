package com.hongbaek.cardreservation

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.hongbaek.cardreservation.R.layout
import splitties.alertdialog.alertDialog
import splitties.alertdialog.message
import splitties.alertdialog.okButton
import splitties.alertdialog.title
import splitties.toast.toast
import java.lang.ClassCastException
import java.util.*

class ScheduleCreateActivity : AppCompatActivity(){
    private val TAG = "ScheduleCreate_A"

    private lateinit var textInputLayout: TextInputLayout
    private lateinit var startTimeLL: LinearLayout
    private lateinit var endTimeLL: LinearLayout
    private lateinit var startPickerLayout: LinearLayout
    private lateinit var endPickerLayout: LinearLayout

    private lateinit var dateTimePickers: DateTimePicker
    private lateinit var datePickers: Pair<DatePicker, DatePicker>
    private lateinit var timePickers: Pair<CustomTimePicker, CustomTimePicker>
    private lateinit var typeSpinner: Spinner

    private lateinit var titleEIT: TextInputEditText
    private lateinit var startDateTV: TextView
    private lateinit var startTimeTV: TextView
    private lateinit var endDateTV: TextView
    private lateinit var endTimeTV: TextView
    private lateinit var passwordIET: TextInputEditText
    private lateinit var estimatedIET: TextInputEditText

    private lateinit var cancelB: Button
    private lateinit var reserveB: Button

    private var startDay = Calendar.getInstance()
    private var endDay = Calendar.getInstance()

    private var onDateChangedListener = DatePicker.OnDateChangedListener { view, year, month, day ->
        when (view.id) {
            R.id.startDatePicker -> {
                startDay.set(year, month, day)
                startDateTV.text = getString(R.string.slashFormattedDate, year, month+1, day)
                Log.d(TAG, "onDateChangedListener: TV.text: ${startDateTV.text}")
            }
            R.id.endDatePicker -> {
                endDay.set(year, month, day)
                endDateTV.text = getString(R.string.slashFormattedDate, year, month+1, day)
            }
            else -> throw Exception("invalid picker id exception")
        }
        var isStart = view.id == R.id.startDatePicker
        Log.d(TAG, "onDateChangedListener: isStartDate_ $isStart")
        Log.d(TAG, "onDateChangedListener: $year.${month+1}.$day")
    }
    private var onTimeChangedListener = TimePicker.OnTimeChangedListener { view, hourOfDay, minute ->
        when (view.id) {
            R.id.startTimePicker -> {
                startDay.set(Calendar.HOUR_OF_DAY, hourOfDay)
                startDay.set(Calendar.MINUTE, minute)
                var minuteValue = (view as CustomTimePicker).getDisplayedMinutes()
                var minuteT = ""
                var hourT = ""
                hourT = if(hourOfDay<10) "0${hourOfDay}" else "$hourOfDay"
                minuteT = if(minuteValue<10) "0$minuteValue" else "$minuteValue"
                Log.d(TAG, "onTimeChangedListener: hour/minute: $hourT/$minuteT")
                startTimeTV.text = getString(R.string.formattedTime, hourT, minuteT)
            }
            R.id.endTimePicker -> {
                endDay.set(Calendar.HOUR_OF_DAY, hourOfDay)
                endDay.set(Calendar.MINUTE, minute)
                var minuteValue = (view as CustomTimePicker).getDisplayedMinutes()
                var minuteT = ""
                var hourT = ""
                hourT = if(hourOfDay<10) "0${hourOfDay}" else "$hourOfDay"
                minuteT = if(minuteValue<10) "0$minuteValue" else "$minuteValue"
                Log.d(TAG, "onTimeChangedListener: hour/minute: $hourT/$minuteT")
                endTimeTV.text = getString(R.string.formattedTime, hourT, minuteT)
            }
            else -> throw Exception("invalid picker id exception")
        }
        var isStart = view.id == R.id.startTimePicker
        Log.d(TAG, "onTimeChangedListener: isStartTime_ $isStart")
    }

    private val functions: FirebaseFunctions by lazy{
        Firebase.functions
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_schedule_create)
        textInputLayout = findViewById(R.id.titleTIL)
        startTimeLL = findViewById(R.id.startTimeLL)
        endTimeLL = findViewById(R.id.endTimeLL)
        startPickerLayout = findViewById(R.id.startPickerLayout)
        endPickerLayout = findViewById(R.id.endPickerLayout)

        datePickers = Pair(findViewById(R.id.startDatePicker), findViewById(R.id.endDatePicker))
        timePickers = Pair(findViewById<CustomTimePicker>(R.id.startTimePicker), findViewById<CustomTimePicker>(R.id.endTimePicker))
        dateTimePickers = DateTimePicker(datePickers, timePickers)
        typeSpinner = findViewById(R.id.typeSpinner)

        startDateTV = findViewById(R.id.startDayTV)
        startTimeTV = findViewById(R.id.startTimeTV)
        endDateTV = findViewById(R.id.endDayTV)
        endTimeTV = findViewById(R.id.endTimeTV)
        titleEIT = findViewById(R.id.titleIET)
        estimatedIET = findViewById(R.id.estimateIET)
        passwordIET = findViewById(R.id.passwordIET)

        cancelB = findViewById(R.id.cancelButton)
        reserveB = findViewById(R.id.reserveButton)

        var year = intent.getIntExtra("sYear", startDay.get(Calendar.YEAR))
        var month = intent.getIntExtra("sMonth", startDay.get(Calendar.MONTH)+1)
        var date = intent.getIntExtra("sDate", startDay.get(Calendar.DAY_OF_MONTH))

        startDateTV.text = getString(R.string.slashFormattedDate, year, month, date)
        endDateTV.text = getString(R.string.slashFormattedDate, year, month, date)
        startTimeLL.setOnClickListener {
            toggle(endPickerLayout, false)
            toggle(startPickerLayout)
        }
        endTimeLL.setOnClickListener {
            toggle(startPickerLayout, false)
            toggle(endPickerLayout)
        }

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
                this,
                R.array.my_array,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            typeSpinner.adapter = adapter
        }

        var currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        var currentMinute = Calendar.getInstance().get(Calendar.MINUTE)
        currentMinute += (5 - currentMinute % 5)
        dateTimePickers.onDateChangedListener = onDateChangedListener
        dateTimePickers.onTimeChangedListener = onTimeChangedListener
        dateTimePickers.init(year,month,date, currentHour, currentMinute/5)
        cancelB.setOnClickListener {
            finish()
        }
        reserveB.setOnClickListener {
            if(checkReserveItem())
                reserveSchedule()
        }
    }

    fun toggle(view: View){
        if(view.visibility == View.VISIBLE){
            view.visibility = View.GONE
        }else{
            view.visibility = View.VISIBLE
        }
    }
    fun toggle(view: View, toggle:Boolean){
        if(toggle)
            view.visibility = View.VISIBLE
        else
            view.visibility = View.GONE
    }

    fun checkReserveItem(): Boolean{
        var msg = ""
        if(titleEIT.text.isNullOrBlank()){
            msg += "사용 용도"
        }
        if(estimatedIET.text.isNullOrBlank()){
            if(msg!="") msg += "/예상 금액"
            else msg += "예상 금액"
        }
        if(passwordIET.text.isNullOrBlank()){
            if(msg!="") msg += "/비밀번호"
            else msg += "비밀번호"
        }
        if(msg != ""){
            toast("$msg 칸이 비었습니다.")
            return false
        }
        if(!endDay.after(startDay)){
            msg += "종료 시간을 시작 시간 뒤로 설정하세요."
            toast(msg)
            return false
        }
        if(typeSpinner.selectedItemPosition == 0){
            if(endDay.minusOfMinute(startDay)>360){
                msg += "최대 예약 시간을 초과했습니다."
                toast(msg)
                return false
            }
        }
        else if(typeSpinner.selectedItemPosition == 1){
            if(endDay.minusOfMinute(startDay)>2880){
                msg += "최대 예약 시간을 초과했습니다."
                toast(msg)
                return false
            }
        }
        else if(typeSpinner.selectedItemPosition == -1){
            toast("타입을 선택하세요.")
            return false
        }
        else {
            toast("Unknown type data..")
            return false
        }
        return true
    }

    private fun reserveSchedule(){
        val progressBarActivity = ProgressBarActivity(this)
        progressBarActivity.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        progressBarActivity.show()

        pushSchedule(titleEIT.text.toString(), userName = "Anonymous", estimatedIET.text.toString(), passwordIET.text.toString(), typeSpinner.selectedItemPosition+1)
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        val e = task.exception
                        Log.w(TAG, "reserveSchedule:onFailure", e)
                        progressBarActivity.dismiss()
                        toast("An error occurred.")
                        return@OnCompleteListener
                    }
                    Log.d(TAG, "reserveSchedule: receive data, task.result: " + task.result)
                    var msg1 = ""
                    var msg2 = ""
                    if(task.result !=null){
                        if(task.result is Map<*,*>) {
                            Log.d(TAG, "reserveSchedule: analyzing data, task.result type: Map")
                            var hashmap = task.result!! as Map<*,*>
                            if (hashmap.containsKey("message")) {
                                msg1 = "예약 성공"
                                msg2 = "정상적으로 등록되었습니다."
                                toast("Reservation Succesful")
                            } else {
                                try {
                                    msg1 = "예약 실패"
                                    if (task.isComplete)
                                        msg2 = "예약이 거부되었습니다."
                                    else
                                        msg2 = "에러가 발생하였습니다."
                                    msg2 += "\n" + hashmap["error"]
                                } catch (e: Exception) {
                                    msg2 = "예약 실패\n 결과 수신 중 에러가 발생하였습니다.\n${e.message}"
                                }
                            }
                        } else if(task.result is HttpsCallableResult) {
                            Log.d(TAG, "reserveSchedule: analyzing data, task.result type: HttpsCallableResult")
                            var callableResult = task.result!! as HttpsCallableResult
                            var data = callableResult.data
                            if(data is Map<*,*>) {
                                Log.d(TAG, "reserveSchedule: analyzing data, callableResult.data type: Map")
                                var hashmap = data as Map<*, *>
                                if (hashmap.containsKey("message")) {
                                    msg1 = "예약 성공"
                                    msg2 = "정상적으로 등록되었습니다."
                                    toast("Reservation Succesful")
                                } else {
                                    try {
                                        msg1 = "예약 실패"
                                        if (task.isComplete)
                                            msg2 = "예약이 거부되었습니다."
                                        else
                                            msg2 = "에러가 발생하였습니다."
                                        msg2 += "\n" + hashmap["error"]
                                    } catch (e: Exception) {
                                        msg2 = "예약 실패\n 결과 수신 중 에러가 발생하였습니다."
                                    }
                                }
                            } else{
                                Log.d(TAG, "reserveSchedule: analyzing data, callableResult.data type: UnKnown")
                                msg1 = "Error"
                                msg2 = "결과를 확인할 수 없습니다. 새로고침하여 결과를 확인하세요."
                            }
                        }
                    }
                    else{
                        msg1 = "Error"
                        msg2 = "결과를 받아오지 못했습니다. 새로고침하여 결과를 확인하세요."
                    }
                    progressBarActivity.dismiss()
                    alertDialog {
                        this.title = msg1
                        message = msg2
                        okButton {
                            if(msg1 == "예약 성공" || msg1 == "Error")
                                finish()
                        }
                    }.show()
                })
    }

    fun pushSchedule(title: String, userName: String, estimated:String, password:String, type:Int=0): Task<Any?> {

        var startString = calendarToString(startDay)
        var endString = calendarToString(endDay)
        val data = hashMapOf(
                "title" to title,
                "userName" to userName,
                "startTime" to startString,
                "endTime" to endString,
                "password" to password,
                "estimated" to estimated,
                "type" to type
        )
        Log.d(TAG, "pushSchedule: sending data: $data")
        return functions.getHttpsCallable("reserveSchedule").call(data)
                .continueWith { task ->
                    var result:Any? = null
                    try{
                        result = task.result as Map<String, Any?>
                    }catch(e:ClassCastException){
                        try{
                            result = task.result as HttpsCallableResult
                        }catch (e: Exception){
                            Log.e("getHttpsCallable.call", e.toString())
                        }
                    }
                    result
                }
    }

    fun calendarToString(calendar:Calendar): String{
        var string = "" + calendar.get(Calendar.YEAR)
        var month = calendar.get(Calendar.MONTH)
        if(month<9) string += ".0${month+1}"
        else string += ".${month+1}"
        var day = calendar.get(Calendar.DAY_OF_MONTH)
        if(day<10) string += ".0$day"
        else string += ".$day"

        var hour = calendar.get(Calendar.HOUR_OF_DAY)
        if(hour<10) string += " 0$hour"
        else string += " $hour"
        var minute = calendar.get(Calendar.MINUTE)
        if(minute<10) string += ":0$minute"
        else string += ":$minute"

        return string
    }

    /** minusOfMinute - minutes difference of two Calendar time
     * @param cal - Calendar to be Compared
     * @return minutes difference by this.time - cal.time */
    fun Calendar.minusOfMinute(cal: Calendar):Long {
        var cal1 = Calendar.getInstance()
        cal1.timeInMillis=(this.timeInMillis/1000)*1000
        cal1.set(Calendar.SECOND, 0)
        var cal2 = Calendar.getInstance()
        cal2.timeInMillis=(cal.timeInMillis/1000)*1000
        cal2.set(Calendar.SECOND, 0)

        if(cal1.after(cal2)){ // cal1 > cal2
            var dif = (cal1.timeInMillis - cal2.timeInMillis)/60000
            Log.d(TAG, "Calendar.minusofminute - $dif")
            return dif
        }
        else if(cal1.before(cal2)){ // cal2 > cal1
            var dif = (cal2.timeInMillis - cal1.timeInMillis)/60000
            Log.d(TAG, "Calendar.minusofminute - $dif")
            return dif * -1
        }
        return 0
    }
}