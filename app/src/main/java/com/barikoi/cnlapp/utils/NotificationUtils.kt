package com.barikoi.cnlapp.utils

import android.R
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import java.io.File

object NotificationUtils {

    @SuppressLint("MissingPermission")
    fun showNotification(context: Context, message: String, fileUri: Uri) {
        val channelId = "download_channel"
        val channelName = "Download Notifications"
        val notificationId = 1

        // Create a NotificationChannel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Create an Intent to open the file
        val openFileIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, "application/octet-stream")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openFileIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.stat_sys_download_done)
            .setContentTitle("Download Complete")
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Show the notification
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    fun showNotification(context: Context, title: String, message: String, fileUri: String, mimeType: String) {
        val channelId = "download_channel"
        val channelName = "Download Notifications"
        val notificationId = 1

        // Create a NotificationChannel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            if (notificationManager?.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for completed downloads"
                }
                notificationManager?.createNotificationChannel(channel)
            }
        }

        val fileUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider", // This must match the authority in the manifest
            File(fileUri)
        )

        // Create an Intent to open the file
        val openFileIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, mimeType)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openFileIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.stat_sys_download_done)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Show the notification
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    fun openFileWithUri(context: Context, file: File) {
        val fileUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider", // This must match the authority in the manifest
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No application found to open this file", Toast.LENGTH_SHORT).show()
        }
    }
}