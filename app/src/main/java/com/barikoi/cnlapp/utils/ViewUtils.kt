package com.barikoi.cnlapp.utils


import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.provider.Settings
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import com.android.volley.NoConnectionError
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.barikoi.barikoitrace.BarikoiTrace
import com.barikoi.barikoitrace.TraceMode
import com.barikoi.cnlapp.order_create.Callback.DialogListener
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.callback.LocationFetch
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import io.sentry.Sentry
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException

object ViewUtils {

    private fun showGPSDisabledAlertToUser(mContext: Context) {
        val alertDialogBuilder = AlertDialog.Builder(mContext)
        alertDialogBuilder.setTitle("GPS Disabled")
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
            .setCancelable(false)
            .setPositiveButton(
                "Goto Settings Page To Enable GPS"
            ) { _, _ ->
                val callGPSSettingIntent = Intent(
                    Settings.ACTION_LOCATION_SOURCE_SETTINGS
                )
                mContext.startActivity(callGPSSettingIntent)
            }
        alertDialogBuilder.setNegativeButton(
            "Cancel"
        ) { dialog, _ -> dialog.cancel() }
        val alert = alertDialogBuilder.create()
        alert.show()
    }

    fun viewDialog(
        mContext: Context,
        message: String,
        styleString: SpannableStringBuilder,
        listener: DialogListener
    ) {
        val dialog = Dialog(mContext)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_dialog)
        val tvMessage: TextView = dialog.findViewById(R.id.tvMessage)
        val btnConfirm = dialog.findViewById<AppCompatButton>(R.id.btn_confirm)
        val btnNo = dialog.findViewById<AppCompatButton>(R.id.btn_no)

        if (message.isNotEmpty()) {
            tvMessage.text = message
        } else {
            tvMessage.setText(styleString, TextView.BufferType.SPANNABLE)
        }

        btnConfirm.setOnClickListener {
            listener.onConfirmed()
            dialog.dismiss()
        }
        btnNo.setOnClickListener {
            listener.onCanceled()
            dialog.dismiss()
        }

        dialog.show()
        val window = dialog.window
        window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

    }

    fun viewDialogCustom(mContext: Context, message: String, listener: DialogListener) {
        val dialog = Dialog(mContext)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_dialog)
        val tvMessage: TextView = dialog.findViewById(R.id.tvMessage)
        val btnConfirm = dialog.findViewById<AppCompatButton>(R.id.btn_confirm)
        val btnNo = dialog.findViewById<AppCompatButton>(R.id.btn_no)

        tvMessage.text = message

        btnConfirm.setOnClickListener {
            listener.onConfirmed()
            dialog.dismiss()
        }
        btnNo.setOnClickListener {
            listener.onCanceled()
            dialog.dismiss()
        }

        dialog.show()
        val window = dialog.window
        window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

    }

    fun viewDialogResponse(mContext: Context, message: String, listener: DialogListener) {
        val dialog = Dialog(mContext)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_dialog_response)
        val tvMessage: TextView = dialog.findViewById(R.id.tvMessage)
        val btnOk = dialog.findViewById<AppCompatButton>(R.id.btn_ok)
        tvMessage.text = message

        btnOk.setOnClickListener {
            listener.onConfirmed()
            dialog.dismiss()
        }

        dialog.show()
        val window = dialog.window
        window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

    }

    fun getErrorResponse(error: VolleyError, mContext: Context) {
        if (error is TimeoutError) {
            Toast.makeText(
                mContext,
                mContext.getString(R.string.request_timeout_check_your_internet_connection_or_contact_admin),
                Toast.LENGTH_LONG
            ).show()
        }
        if (error is NoConnectionError) {
            Toast.makeText(
                mContext,
                "Turn on your internet connection and Try again",
                Toast.LENGTH_LONG
            ).show()
        }
        if (error.networkResponse != null) {
            try {
                val s = String(error.networkResponse.data)
                Log.d("Routes", "message: $s")
                val data = JSONObject(s)
                Toast.makeText(mContext, data.getString("message"), Toast.LENGTH_LONG).show()
            } catch (e: UnsupportedEncodingException) {
                Sentry.captureException(e)
                e.printStackTrace()
            } catch (e: JSONException) {
                Sentry.captureException(e)
                Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    fun getLocation(mContext: Context, activity: Activity, mListener: LocationFetch) {
        val mFusedLocationClient: FusedLocationProviderClient?
        val locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)
            Log.e("location", "fused location: $mFusedLocationClient")
            if (ActivityCompat.checkSelfPermission(
                    mContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    mContext, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
            }
            val cancellationTokenSource = CancellationTokenSource()
            mFusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                object : CancellationToken() {
                    override fun onCanceledRequested(onTokenCanceledListener: OnTokenCanceledListener): CancellationToken {
                        return cancellationTokenSource.token
                    }

                    override fun isCancellationRequested(): Boolean {
                        return false
                    }
                }).addOnSuccessListener { location ->
                if (!location.toString().equals("null")) {
                    if (!location!!.latitude.isNaN()) {
                        if (!location.isFromMockProvider) {
                            mListener.onFetchSuccess(location)
                        } else {
                            mListener.onFailure()
                            Toast.makeText(
                                mContext,
                                "Disable mock location",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    mListener.onFailure()
                    Toast.makeText(
                        mContext, "Location not available \$location", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            mListener.onFailure()
            showGPSDisabledAlertToUser(activity)
        }
    }

    fun startTracking(activity: Activity, mContext: Context) {
        if (BarikoiTrace.isBatteryOptimizationEnabled()) {
            AppLogger.log("BarikoiTrace " + " is batteryOptimized: " + BarikoiTrace.isBatteryOptimizationEnabled())
            BarikoiTrace.requestDisableBatteryOptimization(mContext)
        }
        if (BarikoiTrace.isLocationTracking()) {
            AppLogger.log("BarikoiTrace " + "is tracking 3: " + BarikoiTrace.isLocationTracking())
            Toast.makeText(
                mContext,
                "Service already running!! no need to start again",
                Toast.LENGTH_SHORT
            ).show()
        } else if (!BarikoiTrace.isLocationPermissionsGranted()) {
            AppLogger.log("BarikoiTrace " + "is Location: " + BarikoiTrace.isLocationPermissionsGranted())
            BarikoiTrace.requestLocationPermissions(activity)
        } else if (!BarikoiTrace.isLocationSettingsOn()) {
            AppLogger.log("BarikoiTrace " + "is location settings on: " + BarikoiTrace.isLocationSettingsOn())
            BarikoiTrace.requestLocationServices(activity)
        } else {
            BarikoiTrace.startTracking(TraceMode.Builder().setUpdateInterval(10).build())
            AppLogger.log("BarikoiTrace " + "is tracking 2: " + BarikoiTrace.isLocationTracking())
            if (BarikoiTrace.isLocationTracking()) {
                Toast.makeText(mContext, "Service started!!", Toast.LENGTH_SHORT).show()
                AppLogger.log("BarikoiTrace" + "is tracking")
            }
        }
    }
}