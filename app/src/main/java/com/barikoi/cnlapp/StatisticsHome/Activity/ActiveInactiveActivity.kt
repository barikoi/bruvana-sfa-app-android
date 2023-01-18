package com.barikoi.cnlapp.StatisticsHome.Activity

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.StatisticsHome.Adapter.ActiveInactiveAdapter
import com.barikoi.cnlapp.StatisticsHome.Model.ActiveInactiveSO
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import kotlinx.android.synthetic.main.activity_active_inactive.*
import kotlinx.android.synthetic.main.activity_active_inactive.btnBack
import kotlinx.android.synthetic.main.activity_active_inactive.tvTitle
import kotlinx.android.synthetic.main.activity_order_summary_to.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ActiveInactiveActivity : AppCompatActivity() {
    var token : String? = null
    var user_id : String? = null
    var employeeId: String? = ""
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var queue: RequestQueue? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_inactive)
        queue = RequestQueueSingleton.getInstance(applicationContext).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        editor = prefs!!.edit()
        token = prefs!!.getString(Api.TOKEN, "")
        user_id = prefs!!.getString(Api.USER_ID, "")
        employeeId = prefs!!.getString(Api.EMPLOYEE_ID, "")

        btnBack.setOnClickListener {
            onBackPressed()
            finish()
        }

        val status = intent.getStringExtra("so_status")
        if (status.equals("active", true)){
            tvTitle.setText(resources.getString(R.string.active_so_list))
        }else if (status.equals("inactive", true)){
            tvTitle.setText(resources.getString(R.string.inactive_so_list))
        }

        getListSO(status)
    }

    private fun getListSO(status: String?) {
        val itemList : ArrayList<ActiveInactiveSO> = ArrayList()
        val today = Calendar.getInstance().time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        ApiServices.apiGET(
            Api.get_attendance+"?start_date="+df.format(today)+"&end_date="+df.format(today)+"&with_active_inactive_so=1", queue!!, token!!, object : ApiServiceListener{
                override fun onResponseSuccess(response: String) {
                    try {
                        if (response != null){
                            val obj = JSONObject(response)
                            var attendanceArray : JSONArray? = null
                            if (status.equals("active", true)){
                                attendanceArray = obj.getJSONArray("active")
                            }else if (status.equals("inactive", true)){
                                attendanceArray = obj.getJSONArray("inactive")
                            }
                            itemList.clear()
                            if (attendanceArray!!.length() > 0){
                                for (i in 0 until attendanceArray!!.length()){
                                    var latitude: Double= 0.0
                                    var longitude: Double= 0.0
                                    var imageUrl = "null"
                                    val attendanceobj = attendanceArray.getJSONObject(i)
                                    if (!attendanceobj.isNull("latitude")){
                                        latitude = attendanceobj.getDouble("latitude")
                                    }
                                    if (!attendanceobj.isNull("longitude")){
                                        longitude = attendanceobj.getDouble("longitude")
                                    }
                                    if (attendanceobj.has("images") && !attendanceobj.isNull("images")){
                                        val imageArray = attendanceobj.getJSONArray("images")
                                        if (imageArray.length() > 0){
                                            val imageobj = imageArray.getJSONObject(0)
                                            if (imageobj.has("image_url")){
                                                imageUrl = imageobj.getString("image_url")
                                            }
                                        }
                                    }
                                    itemList.add(
                                        ActiveInactiveSO(
                                            attendanceobj.getString("user_name"),
                                            attendanceobj.getString("user_id"),
                                            status!!, attendanceobj.getString("updated_at"),
                                            attendanceobj.getString("checkin_address"),
                                            latitude,
                                            longitude,
                                            imageUrl
                                    )
                                    )
                                }

                            }

                            val adapter = ActiveInactiveAdapter(itemList)
                            soList.adapter = adapter
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
                    TODO("Not yet implemented")
                }

            })
    }
}