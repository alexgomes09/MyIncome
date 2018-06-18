package com.agomes.myincome.view

import android.content.Context
import android.content.DialogInterface
import android.graphics.Rect
import android.graphics.drawable.Animatable
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.agomes.myincome.MyIncomeApplication
import com.agomes.myincome.R
import com.agomes.myincome.db.IncomeSchema
import com.agomes.myincome.util.Constants
import com.agomes.myincome.util.PreferenceUtil
import com.agomes.myincome.view.custom.DateTimePickerDialog
import kotlinx.android.synthetic.main.activity_main.*
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Period

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
            resetViewAndDataState(false)
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

        list_item.setOnTouchListener({ v: View, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(0.8f).scaleY(0.8f).setDuration(200).start()
            } else if (event.action == MotionEvent.ACTION_UP) {
                v.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
            }
            false
        })

        list_item.setOnClickListener({
            supportFragmentManager.beginTransaction().replace(android.R.id.content, IncomeListFragment()).addToBackStack(null).commit()
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

    override fun onBackPressed() {
        if(supportFragmentManager.backStackEntryCount > 0){
            super.onBackPressed()
            return
        }

        (supportFragmentManager.backStackEntryCount < 0).let {
            AlertDialog.Builder(this)
                    .setTitle("EXIT ??")
                    .setMessage("Do you want to exit the app")
                    .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, id ->
                        super.onBackPressed();
                    })
                    .setNegativeButton("No", DialogInterface.OnClickListener { dialog, id ->
                        dialog.dismiss()
                    })
                    .create().show()
        }
    }

    private fun showTimePickerDialog(settingStartTime: Boolean) {
        val fragment = DateTimePickerDialog()

        if (!settingStartTime && startTime != null) {
            val bundle = Bundle()
            bundle.putLong("endTime", startTime!!.millis)
            fragment.arguments = bundle
        }

        fragment.setDateTimeListener(settingStartTime, object : DateTimePickerDialog.OnDateTimeSetListener {
            override fun onStartTimeSet(st: DateTime) {

                startTime = st
                pick_start_time.text = Html.fromHtml("Work started at: <b>" + convert24to12hour(startTime) + "</b>")

                resetViewAndDataState(true)
            }

            override fun onEndTimeSet(et: DateTime) {

                endTime = et
                pick_end_time.text = Html.fromHtml("Work ended at: <b>" + convert24to12hour(endTime) + "</b>")

                if (endTime?.isBefore(startTime)!!) {
//                        endTime = endTime.plusDays(1)
                    Toast.makeText(this@MainActivity, "End time must be greater than start time", Toast.LENGTH_LONG).show()
                    btn_save.animate()
                            .alpha(0f)
                            .translationY(-50f)
                            .setDuration(500)
                            .withStartAction({ btn_save.isEnabled = false })
                            .withEndAction({ btn_save.visibility = View.GONE })
                    return
                }
                calculateTotalHoursWorked()
            }
        })

        fragment.show(supportFragmentManager, null)
    }

    private fun calculateTotalHoursWorked() {

        if (endTime == null) return

        val workedHours = Period(startTime, endTime).hours
        val workedMinutes = Period(startTime, endTime).minutes

        var textToShow: String

        if (workedHours <= 0) {
            textToShow = "You worked: <b>" + workedMinutes + "min </b> today"
        } else if (workedMinutes <= 0) {
            textToShow = "You worked: <b>" + workedHours + "hr </b> today"
        } else {
            textToShow = "You worked: <b>" + workedHours + "hr " + workedMinutes + "min </b> today "
        }

        salaryEarned = "%.2f".format(Duration(startTime, endTime).standardMinutes * (PreferenceUtil.readPreferenceValue(Constants.salaryMultiplier, 0f) / 60)).toFloat()
        textToShow = textToShow + " and earned <b>$" + salaryEarned + "</b>"

        total_hours_worked.text = Html.fromHtml(textToShow)

        btn_save.animate()
                .alpha(1f)
                .translationY(50f)
                .setDuration(500)
                .withStartAction({ btn_save.visibility = View.VISIBLE })
                .withEndAction { btn_save.isEnabled = true }
    }

    private fun convert24to12hour(dateTime: DateTime?): String {
        return dateTime!!.toString("hh:mm a | dd MMM")
    }

    private fun resetViewAndDataState(resetEnd: Boolean) {
        if (resetEnd) {
            total_hours_worked.text = ""
            pick_end_time.text = "Work ended at: PICK A TIME"
            endTime = null
            btn_save.alpha = 0f
            btn_save.isEnabled = false
            btn_save.translationY = -50f
        } else {
            total_hours_worked.text = ""
            pick_start_time.text = "Work started at: PICK A TIME"
            pick_end_time.text = "Work ended at: PICK A TIME"
            btn_save.alpha = 0f
            btn_save.isEnabled = false
            btn_save.translationY = -50f
            startTime = null
            endTime = null
        }
    }

    private fun saveToDB() {

        realm.executeTransactionAsync(
                {
                    val income = it.createObject(IncomeSchema::class.java)
                    income.date = startTime?.millis
                    income.startTime = startTime?.millis
                    income.endTime = endTime?.millis
                    income.salary = salaryEarned
                    income.salaryMultiplier = PreferenceUtil.readPreferenceValue(Constants.salaryMultiplier, 0f)
                },
                {
                    Toast.makeText(this@MainActivity, "Saved Successfully!", Toast.LENGTH_SHORT).show()
                },
                {
                    Log.v("==TAG==", "MainActivity.saveToDB Error" + it.message)
                    Toast.makeText(this@MainActivity, it.localizedMessage, Toast.LENGTH_SHORT).show()
                })
    }
}