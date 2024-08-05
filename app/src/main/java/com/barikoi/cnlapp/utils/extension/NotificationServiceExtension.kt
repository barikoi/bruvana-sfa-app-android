package com.barikoi.cnlapp.utils.extension


import android.content.Context
import android.content.Intent
import com.barikoi.cnlapp.Activity.MainActivity
import com.barikoi.cnlapp.notification.NotificationActivity
import com.onesignal.notifications.INotificationClickEvent
import com.onesignal.notifications.INotificationClickListener

class NotificationOpenedHandler(private val context: Context) : INotificationClickListener {

    override fun onClick(event: INotificationClickEvent) {
        val notification = event.notification
        val data = notification.additionalData
        val customKey: String? = data?.optString("customKey", null)
        val notKey: String? = data?.optString("type", null)
        if (notKey == "notification") {
            val intent = Intent(context, NotificationActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("customKey", customKey)
            }
            context.startActivity(intent)
        } else {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("customKey", customKey)
            }
            context.startActivity(intent)
        }
    }
}