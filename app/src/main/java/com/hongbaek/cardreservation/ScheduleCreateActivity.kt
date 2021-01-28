package com.hongbaek.cardreservation

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
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.hongbaek.cardreservation.R.layout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import splitties.alertdialog.alertDialog
import splitties.alertdialog.message
import splitties.alertdialog.okButton
import splitties.alertdialog.title
import splitties.toast.toast
import java.util.*

class ScheduleCreateActivity : AppCompatActivity(){
    private lateinit var textInputLayout: TextInputLayout
    private lateinit var startTimeLL: LinearLayout
    private lateinit var endTimeLL: LinearLayout
    private lateinit var startPickerLayout: LinearLayout
    private lateinit var endPickerLayout: LinearLayout

    private lateinit var dateTimePickers: DateTimePicker
    private lateinit var datePickers: Pair<DatePicker, DatePicker>
    private lateinit var timePickers: Pair<CustomTimePicker, CustomTimePicker>

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
                Log.d("SCreateA_", "onDateChangedListener: TV.text: ${startDateTV.text}")
            }
            R.id.endDatePicker -> {
                endDay.set(year, month, day)
                endDateTV.text = getString(R.string.slashFormattedDate, year, month+1, day)
            }
            else -> throw Exception("invalid picker id exception")
        }
        var isStart = view.id == R.id.startDatePicker
        Log.d("SCreateA_", "onDateChangedListener: isStartDate_ $isStart")
        Log.d("SCreateA_", "onDateChangedListener: $year.${month+1}.$day")
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
                Log.d("SCreateA_", "onTimeChangedListener: hour/minute: "+hourT+ minuteT)
                endTimeTV.text = getString(R.string.formattedTime, hourT, minuteT)
            }
            else -> throw Exception("invalid picker id exception")
        }
        var isStart = view.id == R.id.startTimePicker
        Log.d("SCreateA_", "onTimeChangedListener: isStartTime_ $isStart")
        Log.d("SCreateA_", "onTimeChangedListener: $hourOfDay/${(view as CustomTimePicker).getDisplayedMinutes()}")
    }

    private lateinit var functions: FirebaseFunctions

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
                return@setOnClickListener
            }
            var progressBar: ProgressBar = ProgressBar(this@ScheduleCreateActivity)
            progressBar.isIndeterminate = true
            progressBar.visibility = View.VISIBLE
            reserveSchedule(titleEIT.text.toString(), estimatedIET.text.toString(), passwordIET.text.toString())
                    .addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            val e = task.exception
                            Log.w("ScheduleCreateActivity", "reserveSchedule:onFailure", e)
                            progressBar.visibility = View.GONE
                            toast("An error occurred.")
                            return@OnCompleteListener
                        }
                        Log.d("reserveSchedule", "receive data, task.result: " + task.result)
                        toast("Reservation Succesful")
                        var msg1 = ""
                        var msg2 = ""
                        if (task.result?.get("message") != null) {
                            msg1 = "예약 성공"
                            msg2 = "정상적으로 등록되었습니다."
                        } else {
                            msg1 = "예약 실패"
                            if (task.isComplete)
                                msg2 = "예약이 거부되었습니다."
                            else
                                msg2 = "에러가 발생하였습니다. (검토 필요)"
                            msg2 += "\n" + task.result?.get("error")
                        }
                        progressBar.visibility = View.GONE
                        alertDialog {
                            this.title = msg1
                            message = msg2
                            okButton()
                        }.show()
                        finish()
            })
        }

        functions = Firebase.functions

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

    fun reserveSchedule(title: String, estimated:String, password:String): Task<JSONObject> {

            var startString = calendarToString(startDay)
            var endString = calendarToString(endDay)
            val data = hashMapOf(
                    "title" to title,
                    "startTime" to startString,
                    "endTime" to endString,
                    "password" to password,
                    "estimated" to estimated
            )
            return functions.getHttpsCallable("reserveSchedule").call(data)
                    .continueWith { task ->
                        val result = task.result?.data
                        var json:JSONObject
                        if(result == null)
                            json = JSONObject()
                        else json = result as JSONObject
                        json
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
}