package com.agomes.myincome.view

import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Animatable
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import com.agomes.myincome.MyIncomeApplication
import com.agomes.myincome.R
import com.agomes.myincome.db.IncomeSchema
import com.agomes.myincome.util.Constants
import com.agomes.myincome.util.PreferenceUtil
import kotlinx.android.synthetic.main.activity_main.*
import org.joda.time.*
import org.joda.time.format.DateTimeFormat
import kotlin.math.min

@RequiresApi(Build.VERSION_CODES.KITKAT)
class MainActivity : AppCompatActivity() {

    var startTime: DateTime? = null
    var endTime: DateTime? = null
    var salaryEarned: Float = 0f
    val realm = MyIncomeApplication.getRealmInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        et_salary.setText(PreferenceUtil.readPreferenceValue(Constants.salaryMultiplier, 0.0f).toString())

        pick_start_time.setOnClickListener {
            showTimePickerDialog(true)
        }

        pick_end_time.setOnClickListener {
            if (startTime != null) {
                showTimePickerDialog(false)
            } else {
                Toast.makeText(this@MainActivity, "Pick start time", Toast.LENGTH_SHORT).show()
            }
        }

        et_salary.onFocusChangeListener = View.OnFocusChangeListener { p0, p1 ->
            resetViewAndDataState()
            if (!p1 && et_salary.length() > 0) {
                PreferenceUtil.writePreferenceValue(Constants.salaryMultiplier, et_salary.text.toString().toFloat())
                check_mark.alpha = 1f
                (check_mark.drawable as Animatable).start()
                check_mark.animate().alpha(0f).setDuration(2000).start()
            }
        }

        btn_save.setOnClickListener {
            saveToDB()
        }

        list_item.setOnClickListener({
            supportFragmentManager.beginTransaction().replace(android.R.id.content,IncomeListFragment()).addToBackStack(null).commit()
        })
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action === MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    private fun showTimePickerDialog(settingStartTime: Boolean) {

        val timePicker = TimePickerDialog(this@MainActivity, object : TimePickerDialog.OnTimeSetListener {
            override fun onTimeSet(timePicker: TimePicker, hour: Int, minute: Int) {

                if (settingStartTime) {
                    startTime = DateTime().withTime(hour, minute,0,0)
                    pick_start_time.text = Html.fromHtml("Work started at: <b>" + convert24to12hour(startTime) + "</b>")

                    if (endTime != null && endTime?.isBefore(startTime)!!) {
                        Toast.makeText(this@MainActivity, "End time must be greater than start time", Toast.LENGTH_SHORT).show()
                        return
                    }
                    calculateTotalHoursWorked()
                } else {
                    endTime = DateTime().withTime(hour,minute,0,0)

                    pick_end_time.text = Html.fromHtml("Work ended at: <b>" + convert24to12hour(endTime) + "</b>")

                    if (endTime?.isBefore(startTime)!!) {
//                        endTime = endTime.plusDays(1);
                        Toast.makeText(this@MainActivity, "End time must be greater than start time", Toast.LENGTH_SHORT).show()
                        return
                    }
                    calculateTotalHoursWorked()
                }
            }
        }, DateTime.now().hourOfDay, 0, false)

        timePicker.setTitle(if (settingStartTime) {
            "Work start time"
        } else {
            "Work end time"
        })

        timePicker.show()
    }

    private fun calculateTotalHoursWorked() {

        if (endTime == null) return

        val workedHours = Period(startTime, endTime).hours
        val workedMinutes = Period(startTime, endTime).minutes

        var textToShow = "";

        if (workedHours <= 0) {
            textToShow = "You worked: <b>" + workedMinutes + "min </b> today"
        } else if (workedMinutes <= 0) {
            textToShow = "You worked: <b>" + workedHours + "hr </b> today"
        } else {
            textToShow = "You worked: <b>" + workedHours + "hr " + workedMinutes + "min </b> today "
        }

        salaryEarned = "%.2f".format(Duration(startTime, endTime).standardMinutes * (PreferenceUtil.readPreferenceValue(Constants.salaryMultiplier, 0f) / 60)).toFloat()
        textToShow = textToShow + " and earned <b>" + salaryEarned + "</b>"

        total_hours_worked.text = Html.fromHtml(textToShow)

        btn_save.animate().alpha(1f).translationY(50f).setDuration(1000).withEndAction { btn_save.isEnabled = true }
    }

    private fun convert24to12hour(dateTime: DateTime?): String {
        return DateTimeFormat.forPattern("hh:mm a").print(dateTime).toString()
    }

    private fun resetViewAndDataState(){
        total_hours_worked.text = ""
        pick_start_time.text = "Work started at: PICK A TIME"
        pick_end_time.text = "Work ended at: PICK A TIME"
        btn_save.alpha = 0f
        btn_save.isEnabled = false
        btn_save.translationY = -50f
        startTime = null
        endTime = null
    }

    private fun saveToDB() {

        realm.executeTransactionAsync(
                {
                    val income = it.createObject(IncomeSchema::class.java)
                    income.date = DateTime.now().millis
                    income.startTime = startTime?.millis
                    income.endTime = endTime?.millis
                    income.salary = salaryEarned
                    income.salaryMultiplier = PreferenceUtil.readPreferenceValue(Constants.salaryMultiplier, 0f)
                },
                {
                    Toast.makeText(this@MainActivity, "Saved Successfully!", Toast.LENGTH_SHORT).show()
                },
                {
                    Log.v("==TAG==", "MainActivity.saveToDB Error" + it.message);
                    Toast.makeText(this@MainActivity, it.localizedMessage, Toast.LENGTH_SHORT).show()
                })
    }
}
