package com.barikoi.cnlapp.utils.ApiService

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.widget.Toast
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

                            val baseFolder =
                                mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath
                            val file =
                                File(baseFolder + File.separator + "CNL Documents" + File.separator + name)
                            file.parentFile?.mkdirs()
                            val fos = FileOutputStream(file)
                            fos.write(response)
                            fos.close()
                            Toast.makeText(
                                mContext,
                                "Your Download is Complete.",
                                Toast.LENGTH_LONG
                            ).show()
                            enableNotification(mContext, name, file)
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
}