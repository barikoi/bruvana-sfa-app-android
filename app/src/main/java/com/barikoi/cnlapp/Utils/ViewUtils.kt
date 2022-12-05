package com.barikoi.cnlapp.Utils


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import com.android.volley.NoConnectionError
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Order_Create.Callback.DialogListener
import io.sentry.Sentry
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException

object ViewUtils {

    fun showGPSDisabledAlertToUser(mContext: Context) {
        val alertDialogBuilder = AlertDialog.Builder(
            mContext!!
        )
        alertDialogBuilder.setTitle("GPS Disabled")
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
            .setCancelable(false)
            .setPositiveButton(
                "Goto Settings Page To Enable GPS"
            ) { dialog, id ->
                val callGPSSettingIntent = Intent(
                    Settings.ACTION_LOCATION_SOURCE_SETTINGS
                )
                mContext.startActivity(callGPSSettingIntent)
            }
        alertDialogBuilder.setNegativeButton(
            "Cancel"
        ) { dialog, id -> dialog.cancel() }
        val alert = alertDialogBuilder.create()
        alert.show()
    }

    fun viewDialog(mContext: Context, message: String, listener: DialogListener){
        val dialog = Dialog(mContext)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.popup_dialog)
        val tvMessage = dialog.findViewById(R.id.tvMessage) as TextView
        val btnConfirm = dialog.findViewById<AppCompatButton>(R.id.btn_confirm)
        val btnNo = dialog.findViewById<AppCompatButton>(R.id.btn_no)

        tvMessage.setText(message)

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
    fun viewDialogResponse(mContext: Context, message: String, listener: DialogListener){
        val dialog = Dialog(mContext)
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.popup_dialog_response)
        val tvMessage = dialog.findViewById(R.id.tvMessage) as TextView
        val btnOk = dialog.findViewById<AppCompatButton>(R.id.btn_ok)
        tvMessage.setText(message)

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

    fun getErrorResponse(error: VolleyError, mContext: Context){
        if (error is TimeoutError) {
            //mListerner.onFailure("Request timeout!! Check your internet connection or Contact Admin")
            Toast.makeText(mContext, "Request timeout!! Check your internet connection or Contact Admin", Toast.LENGTH_LONG).show()
        }
        if (error is NoConnectionError) {
            //mListerner.onFailure("Turn on your internet connection and Try again")
            Toast.makeText(mContext, "Turn on your internet connection and Try again", Toast.LENGTH_LONG).show()
        }
        if (error != null && error.networkResponse != null) {
            try {
                val s = String(error.networkResponse.data)
                Log.d("Routes", "message: $s")
                val data = JSONObject(s)
                //Toast.makeText(mContext.getApplicationContext(), data.getString("message"), Toast.LENGTH_SHORT).show();
                //mListerner.onFailure(data.getString("message"))
                Toast.makeText(mContext, data.getString("message"), Toast.LENGTH_LONG).show()
            } catch (e: UnsupportedEncodingException) {
                Sentry.captureException(e)
                e.printStackTrace()
            } catch (e: JSONException) {
                //mListerner.onFailure(e.message)
                Sentry.captureException(e)
                Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }
}