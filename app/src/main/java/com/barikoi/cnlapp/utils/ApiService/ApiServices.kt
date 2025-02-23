package com.barikoi.cnlapp.utils.ApiService

import android.Manifest
import android.R.attr.mimeType
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.barikoi.cnlapp.BuildConfig
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.utils.InputStreamVolleyRequest
import com.barikoi.cnlapp.utils.VolleyMultipartRequest
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


object ApiServices {

    private const val NOTIFICATION_ID = 12345
    private const val CHANNEL_ID = "CNL Chalan_Download"

    fun apiPOST(
        url: String,
        queue: RequestQueue,
        token: String,
        parameters: MutableMap<String, String>,
        mListener: ApiServiceListener
    ) {
        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    mListener.onResponseSuccess(response)
                } catch (e: Exception) {
                    mListener.onException(e)
                }
            },
            { error ->
                mListener.onResponseFailure(error)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Accept"] = "application/json"
                if (token != "") {
                    params["Authorization"] = "bearer $token"
                }
                return params
            }

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                return parameters
            }
        }
        request.retryPolicy = DefaultRetryPolicy(
            60 * 1000, 0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(request)
    }

    fun apiJSONObjectPOST(
        url: String,
        queue: RequestQueue,
        token: String,
        jsonObj: JSONObject,
        mListener: ApiServiceListener
    ) {
        val request = object : JsonObjectRequest(
            Method.POST, url, jsonObj,
            { response ->
                try {
                    mListener.onJSONResponseSuccess(response)
                } catch (e: Exception) {
                    mListener.onException(e)
                }
            },
            { error ->
                mListener.onResponseFailure(error)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val parameters: MutableMap<String, String> = HashMap()
                parameters["Accept"] = "application/json"
                if (token != "") {
                    parameters["Authorization"] = "bearer $token"
                }
                return parameters
            }
        }
        request.retryPolicy = DefaultRetryPolicy(
            60 * 1000, 0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(request)
    }

    fun apiPOSTMultipart(
        url: String,
        queue: RequestQueue,
        token: String,
        parameters: MutableMap<String, String>,
        byteParams: MutableMap<String, VolleyMultipartRequest.DataPart>,
        mListener: ApiServiceListener
    ) {
        val request = object : VolleyMultipartRequest(
            Method.POST, url,
            { response ->
                try {
                    mListener.onNetworkResponseSuccess(response)
                } catch (e: Exception) {
                    mListener.onException(e)
                }
            },
            { error ->
                mListener.onResponseFailure(error)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Accept"] = "application/json"
                if (token != "") {
                    params["Authorization"] = "bearer $token"
                }
                return params
            }

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                return parameters
            }

            override fun getByteData(): Map<String, DataPart> {
                return byteParams
            }
        }
        request.retryPolicy = DefaultRetryPolicy(
            60 * 1000, 0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(request)
    }

    fun apiGET(url: String, queue: RequestQueue, token: String, mListener: ApiServiceListener) {
        val request = object : StringRequest(Method.GET, url,
            { response ->
                try {
                    mListener.onResponseSuccess(response)
                } catch (e: Exception) {
                    mListener.onException(e)
                }
            },
            { error ->
                mListener.onResponseFailure(error)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val parameters: MutableMap<String, String> = HashMap()
                parameters["Accept"] = "application/json"
                if (token != "") {
                    parameters["Authorization"] = "bearer $token"
                }
                return parameters
            }
        }
        request.retryPolicy = DefaultRetryPolicy(
            60 * 1000, 0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(request)
    }

    fun apiGETInputStream(
        url: String,
        mContext: Context,
        mListener: ApiServiceListener
    ) {
        val mRequestQueue = Volley.newRequestQueue(
            mContext,
            HurlStack()
        )
        val request =
            object : InputStreamVolleyRequest(
                Method.GET, url,
                { response ->
                    try {
                        if (response != null) {
                            val timeStamp =
                                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
                            val name = "order_chalan_$timeStamp.pdf"

                            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            val filePath = File(downloadsDir, "CNL Documents/$name")


                            savePdfFile(context = mContext, name, response, filePath)
                        }
                    } catch (e: Exception) {
                        mListener.onException(e)
                    }
                },
                { error ->
                    mListener.onResponseFailure(error)
                }, null
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val parameters: MutableMap<String, String> = HashMap()
                    parameters["Accept"] = "application/json"
                    return parameters
                }
            }
        request.retryPolicy = DefaultRetryPolicy(
            60 * 1000, 0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        mRequestQueue.add(request)
    }

    fun savePdfFile(context: Context, fileName: String, fileData: ByteArray, filePath: File) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10 and above, use MediaStore API
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/CNL Documents")
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                if (uri != null) {
                    resolver.openOutputStream(uri).use { outputStream ->
                        if (outputStream != null) {
                            outputStream.write(fileData)
                            Log.d("SavePdfFile", "File saved successfully at $uri")
                        } else {
                            Log.e("SavePdfFile", "Failed to open output stream")
                        }
                    }
                } else {
                    Log.e("SavePdfFile", "Failed to create URI")
                }
            } else {
                // For Android 9 and below, use traditional file storage
                val baseFolder = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                val cnlFolder = File(baseFolder, "CNL Documents")

                if (!cnlFolder.exists()) {
                    cnlFolder.mkdirs()
                }

                val file = File(cnlFolder, fileName)
                FileOutputStream(file).use { outputStream ->
                    outputStream.write(fileData)
                    Log.d("SavePdfFile", "File saved successfully at ${file.absolutePath}")
                }
            }


            Toast.makeText(
                context,
                "Your Download is Complete.",
                Toast.LENGTH_LONG
            ).show()
            enableNotification1(context, fileName, File(filePath.absolutePath))
        } catch (e: Exception) {
            Log.e("SavePdfFile", "Error saving file: ${e.message}", e)
        }
    }

    private fun enableNotification(context: Context, fileName: String, filePath: File) {
        val notificationManager = NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "$fileName is completed.",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_download)
            .setContentTitle("Downloaded")
            .setAutoCancel(true)
            .setContentText("$fileName is completed.")

        val uri = FileProvider.getUriForFile(
            context,
            BuildConfig.APPLICATION_ID + ".fileprovider",
            filePath
        )
        val targetIntent = Intent()
        targetIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        targetIntent.setAction(Intent.ACTION_VIEW)
        targetIntent.setDataAndType(uri, "application/pdf")

        val contentIntent =
            PendingIntent.getActivity(
                context,
                0,
                targetIntent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_NO_CREATE
            )
        builder.setContentIntent(contentIntent)
        val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun enableNotification1(context: Context, fileName: String, filePath: File) {
        val notificationManager = NotificationManagerCompat.from(context)
        val channelId = "DOWNLOAD_CHANNEL_ID"
        val notificationId = 101

        // Create Notification Channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Download Notifications",
                NotificationManager.IMPORTANCE_HIGH // High importance for visibility
            ).apply {
                description = "Notifications for completed downloads."
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Prepare File URI using FileProvider
        val uri = FileProvider.getUriForFile(
            context,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            filePath
        )

        // Create an Intent to open the file
        val openFileIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        // PendingIntent for the notification action
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            openFileIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_download) // Use a relevant icon
            .setContentTitle("Download Complete")
            .setContentText("$fileName has been downloaded successfully.")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // High priority for pre-Oreo
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}