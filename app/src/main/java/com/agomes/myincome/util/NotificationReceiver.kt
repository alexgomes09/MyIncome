package com.agomes.myincome.util

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.agomes.myincome.R
import com.agomes.myincome.view.MainActivity

/**
 * Created by agomes on 4/21/18.
 */
class NotificationReceiver : BroadcastReceiver() {

    lateinit var alarmManager: AlarmManager

    override fun onReceive(context: Context, intent: Intent) {

        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.cancel(pendingIntent)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pendingIntent)

        showNotification(context)
    }

    fun showNotification(context: Context){
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(context)
        builder.setContentTitle(context.getString(R.string.app_name))
        builder.setContentText("Did you work today")
        builder.setSmallIcon(R.mipmap.ic_launcher)

        notificationManager.notify(2, builder.build())
    }
}