package com.barikoi.cnlapp.utils.extension


import android.content.Context
import android.content.Intent
import com.barikoi.cnlapp.ui.approval.StockRequestApprovalActivity
import com.barikoi.cnlapp.ui.notification.NotificationActivity
import com.barikoi.cnlapp.ui.request.StockRequestActivity
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.Constants
import com.onesignal.notifications.INotificationClickEvent
import com.onesignal.notifications.INotificationClickListener

class NotificationOpenedHandler(private val context: Context) : INotificationClickListener {

    override fun onClick(event: INotificationClickEvent) {
        AppLogger.log("Notification Clicked ${event.notification}")
        val notification = event.notification
        val data = notification.additionalData
        AppLogger.log("Notification Clicked ${data}")
        val customKey: String? = data?.optString("customKey", null)
        val userType: String? = data?.optString("type", "")
        val requestType: String? = data?.optString("request_type", "")
        val notKey: String? = data?.optString("type", null)

        if (userType == "SO") {
            val intent = Intent(context, StockRequestActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(Constants.REQUEST_TYPE, requestType)
            }
            context.startActivity(intent)
        } else if (userType == "TO") {
            val intent = Intent(context, StockRequestApprovalActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(Constants.REQUEST_TYPE, requestType)
            }
            context.startActivity(intent)
        } else {
            val intent = Intent(context, NotificationActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(Constants.REQUEST_TYPE, requestType)
            }
            context.startActivity(intent)
        }
    }
}