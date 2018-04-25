package com.agomes.myincome.view.custom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import com.agomes.myincome.R
import kotlinx.android.synthetic.main.date_time_picker_dialog.*
import org.joda.time.DateTime

/**
 * Created by agomes on 4/23/18.
 */
class DateTimePickerDialog : android.support.v4.app.DialogFragment() {

    var dtListener: OnDateTimeSetListener? = null
    var startTime: DateTime = DateTime.now().withSecondOfMinute(0).withMillisOfSecond(0)
    var endTime = startTime

    var year = startTime.year
    var month = startTime.monthOfYear
    var day = startTime.dayOfMonth
    var hour = startTime.hourOfDay
    var minute = startTime.minuteOfHour
    var isSettingStartTime: Boolean = true

    interface OnDateTimeSetListener {
        fun onStartTimeSet(st: DateTime)
        fun onEndTimeSet(et: DateTime)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.date_time_picker_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog_title.text = if (isSettingStartTime) "Work start time" else "Work end time"

        btn_cancel.setOnClickListener { dialog.dismiss() }

        btn_set.setOnClickListener {
            if (isSettingStartTime) {
                startTime = startTime.withDate(year, month, day).withHourOfDay(hour).withMinuteOfHour(minute)
                dtListener?.onStartTimeSet(startTime)
            } else {
                endTime = DateTime(arguments?.getLong("endTime")).withTime(hour, minute,0,0)
                dtListener?.onEndTimeSet(endTime)
            }

            dialog.dismiss()
        }

        timePicker.setOnTimeChangedListener(object : TimePicker.OnTimeChangedListener {
            override fun onTimeChanged(view: TimePicker?, hourOfDay: Int, min: Int) {
                hour = hourOfDay
                minute = min
            }
        })

        if(!isSettingStartTime){
            datePicker.visibility = View.GONE
            return
        }

        datePicker.init(year, month.minus(1), day, object : DatePicker.OnDateChangedListener {
            override fun onDateChanged(view: DatePicker, y: Int, monthOfYear: Int, dayOfMonth: Int) {
                year = y
                month = monthOfYear.plus(1)
                day = dayOfMonth
            }
        })
    }

    fun setDateTimeListener(iSSTime: Boolean, dtListener: OnDateTimeSetListener) {
        isSettingStartTime = iSSTime
        this.dtListener = dtListener
    }

    override fun onDestroy() {
        super.onDestroy()
        this.dtListener = null
    }
}