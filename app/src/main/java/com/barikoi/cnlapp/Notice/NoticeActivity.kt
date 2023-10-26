package com.barikoi.cnlapp.Notice

import android.app.Dialog
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.preference.PreferenceManager
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.Order_Create.Callback.DialogListener
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.activity_notice.*
import kotlinx.android.synthetic.main.activity_notice.btnBack
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class NoticeActivity : AppCompatActivity() {
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var queue: RequestQueue? = null
    private var token : String?= ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)

        queue = RequestQueueSingleton.getInstance(applicationContext).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        editor = prefs!!.edit()
        token = prefs!!.getString(Api.TOKEN, "")
        if (prefs!!.getString(Api.USER_TYPE, "").equals("TO", true)){
            fab_create_notice.visibility = View.VISIBLE
        }else{
            fab_create_notice.visibility = View.GONE
        }
        btnBack.setOnClickListener {
            onBackPressed()
            finish()
        }
        fab_create_notice.setOnClickListener {
            createNoticePopUp()
        }
        setDateFilter()
    }

    private fun createNoticePopUp() {
        val dialog = Dialog(this@NoticeActivity)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.popup_create_notice)
        val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)
        val btnSubmit = dialog.findViewById<AppCompatButton>(R.id.btnSubmit)
        val etNotice = dialog.findViewById<EditText>(R.id.editTextNotice)

        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        btnSubmit.setOnClickListener {
            if (etNotice.text.trim().length > 0) {
                val obj1 = JSONObject()
                val noticesArray = JSONArray()
                val noticeObj = JSONObject()
                noticeObj.put("message", etNotice.text.toString())
                val designationArray = JSONArray()
                /*for loop here*/
                val designationObj = JSONObject()
                designationObj.put("designation", "SO")
                designationArray.put(designationObj)
                noticeObj.put("designations", designationArray)
                noticesArray.put(noticeObj)
                obj1.put("notices", noticesArray)
                submitNotice(obj1, dialog)
            }else{
                Toast.makeText(applicationContext, "Type notice first to submit.", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
        val window = dialog.window
        window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun submitNotice(objNotice: JSONObject, dialog: Dialog) {
        ApiServices.apiJSONObjectPOST(Api.submit_notice, queue!!, token!!, objNotice, object : ApiServiceListener{
            override fun onResponseSuccess(response: String) {
                TODO("Not yet implemented")
            }

            override fun onJSONResponseSuccess(response: JSONObject) {
                try {
                    val message = response.getString("message")
                    ViewUtils.viewDialogResponse(this@NoticeActivity, message, object : DialogListener {
                        override fun onConfirmed() {
                            setDateFilter()
                            dialog.dismiss()
                        }

                        override fun onCanceled() {
                            TODO("Not yet implemented")
                        }

                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onNetworkResponseSuccess(response: NetworkResponse) {
                TODO("Not yet implemented")
            }

            override fun onResponseFailure(error: VolleyError) {
                ViewUtils.getErrorResponse(error, applicationContext)
            }

            override fun onException(e: Exception) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun setDateFilter() {
        val c = Calendar.getInstance()
        //c.add(Calendar.DAY_OF_WEEK, -7)
        c.set(Calendar.DAY_OF_MONTH, 1);
        val end = Calendar.getInstance().time
        val start = c.time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val simpleFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)
        val StartDate = df.format(start)
        val EndDate = df.format(end)

        tvDateRange.setText(/*simpleFormat.format(start) + " - " + */simpleFormat.format(end))


        val materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker()
        materialDateBuilder.setTheme(R.style.ThemeOverlay_App_MaterialCalendar)
        materialDateBuilder.setTitleText("SELECT A DATE")

        val materialDatePicker = materialDateBuilder.build()

        dateRangeLayout.setOnClickListener(View.OnClickListener {
            materialDatePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
            dateRangeLayout.setEnabled(false)
        })

        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            dateRangeLayout.setEnabled(true)
            val s_date = Date(selection.first!!)
            val e_date = Date(selection.second!!)
            if (s_date.compareTo(e_date) == 0) {
                tvDateRange.setText(simpleFormat.format(s_date))
            } else {
                tvDateRange.setText(simpleFormat.format(s_date) + " - " + simpleFormat.format(e_date))
            }
            getNoticeList(Api.get_notice+"?start_date="+df.format(s_date)+"&end_date="+df.format(e_date))

        }

        materialDatePicker.addOnNegativeButtonClickListener { dateRangeLayout.setEnabled(true) }

        getNoticeList(Api.get_notice+"?start_date="+StartDate+"&end_date="+EndDate)
    }

    private fun getNoticeList(url: String) {

        ApiServices.apiGET(url, queue!!, token!!, object : ApiServiceListener {
            override fun onResponseSuccess(response: String) {
                try {
                    if (response != null){
                        val itemList: ArrayList<Notice> = ArrayList()
                        val obj = JSONObject(response)
                        val noticeArray = obj.getJSONArray("notices")
                        if (noticeArray.length() > 0){
                            for (i in 0 until noticeArray.length()){
                                val noticeObj = noticeArray.getJSONObject(i)
                                itemList.add(
                                    Notice(
                                        noticeObj.getString("id"),
                                        noticeObj.getString("message"),
                                        noticeObj.getString("updated_at"),
                                        noticeObj.getString("name"),
                                        noticeObj.getString("designation"),
                                        noticeObj.getString("image")
                                    )
                                )
                            }
                        }


                        val adapter = NoticeListAdapter(itemList)
                        noticeList.adapter = adapter
                        adapter!!.notifyDataSetChanged()


                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }

            override fun onJSONResponseSuccess(response: JSONObject) {
                TODO("Not yet implemented")
            }

            override fun onNetworkResponseSuccess(response: NetworkResponse) {
                TODO("Not yet implemented")
            }

            override fun onResponseFailure(error: VolleyError) {
                ViewUtils.getErrorResponse(error, applicationContext)
            }

            override fun onException(e: Exception) {
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            }

        })
    }
}