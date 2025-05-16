package com.example.calendarik

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventName = intent.getStringExtra("eventName") ?: "Event"
        val noteId = intent.getLongExtra("noteId", System.currentTimeMillis())

        val notification = NotificationCompat.Builder(context, "event_channel")
            .setSmallIcon(R.drawable.ic_notifications) // замените на свою иконку
            .setContentTitle("Event Reminder")
            .setContentText("Don't forget about $eventName!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Используем модульность ID, чтобы избежать конфликтов
        val notificationId = (noteId % Integer.MAX_VALUE).toInt()
        notificationManager.notify(notificationId, notification)
    }
}