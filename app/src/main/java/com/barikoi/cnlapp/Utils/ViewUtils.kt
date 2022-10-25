package com.barikoi.cnlapp.Utils


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.callback.DialogListener

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
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_popup)
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
}