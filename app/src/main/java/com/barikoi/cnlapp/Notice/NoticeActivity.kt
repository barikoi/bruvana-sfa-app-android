package com.barikoi.cnlapp.Notice

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Toast
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.activity_notice.*
import kotlinx.android.synthetic.main.activity_notice.btnBack
import kotlinx.android.synthetic.main.activity_trade_offers.*
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
        btnBack.setOnClickListener {
            onBackPressed()
            finish()
        }
        setDateFilter()
    }

    private fun setDateFilter() {
        val c = Calendar.getInstance()
        //c.add(Calendar.DAY_OF_WEEK, -7)
        c.set(Calendar.DAY_OF_MONTH, 1);
        val end = Calendar.getInstance().time
        val start = c.time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val simpleFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val StartDate = df.format(start)
        val EndDate = df.format(end)

        tvDateRange.setText(simpleFormat.format(start) + " - " + simpleFormat.format(end))


        val materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker()
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