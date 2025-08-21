package com.barikoi.cnlapp.ui.attendance.fragment.to

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.Attendance.Adapter.TO.HistoryListTOAdapter
import com.barikoi.cnlapp.Attendance.Model.HistoryList
import com.barikoi.cnlapp.Attendance.Model.SOList
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.data.remote.models.To
import com.barikoi.cnlapp.databinding.FragmentHistoryTOBinding
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.utils.ApiService.ApiServices
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.ViewUtils
import com.barikoi.cnlapp.utils.extension.formatDate
import com.barikoi.cnlapp.utils.extension.toast
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class HistoryTOFragment : Fragment() {
    private lateinit var binding: FragmentHistoryTOBinding


    private val viewModel: HistoryTOViewModel by viewModels()

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage


    private var toList: List<To> = emptyList()

    @Inject
    lateinit var mQueue: RequestQueue

    var historyList: ArrayList<HistoryList> = ArrayList()

    var adapter: HistoryListTOAdapter? = null

    var selected_so: Int? = null
    var selected_so_id: String? = null
    val soList: ArrayList<SOList> = ArrayList()
    val filteredsoList: ArrayList<HistoryList> = ArrayList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryTOBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        startToObserve()


        binding.spinnerSO.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (binding.spinnerSO.adapter.count > 0) {
                    if (sharePrefUtils.getString(Api.USER_TYPE).equals("ASM")) {
                        selected_so = p2
                        if (p2 > 0) {
                            selected_so_id = toList[p2 - 1].toId.toString()
                        }
                        if (p2 == 0) {
                            setDateFilter("&with_asm=1")
                        } else {
                            setDateFilter("")
                        }
                    } else {
                        selected_so = p2
                        if (p2 > 0) {
                            selected_so_id = soList[p2 - 1].id
                        }
                        if (p2 == 0) {
                            setDateFilter("&with_to=1")
                        } else {
                            setDateFilter("")
                        }
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}

        }
    }

    private fun getSOList() {
        ApiServices.apiGET(
            Api.get_all_so_list,
            mQueue, sharePrefUtils.getString(Api.TOKEN)!!, object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    viewSOList(response)
                }

                override fun onJSONResponseSuccess(response: JSONObject) {}

                override fun onNetworkResponseSuccess(response: NetworkResponse) {}

                override fun onResponseFailure(error: VolleyError) {
                    ViewUtils.getErrorResponse(error, requireContext())
                }

                override fun onException(e: Exception) {
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun startToObserve() {
        lifecycleScope.launch {
            viewModel.toResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startToObserve::Empty")

                    }

                    is ApiState.Error -> {
                        AppLogger.log("startToObserve::Error ${it.error}")

                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startToObserve::Loading")

                    }

                    is ApiState.Success -> {
                        AppLogger.log("startToObserve:: Success ${it.data}")
                        if (it.data?.toList.isNullOrEmpty()) {
                            toast("To list empty")
                            return@observe
                        }

                        toList = it.data?.toList ?: emptyList()
                        val toNameList = it.data?.toList?.map { to -> to.toName }!!.toMutableList()
                        toNameList.add(0, "${sharePrefUtils.getString(Api.NAME)} (You)")

                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item, toNameList.toMutableList()
                        )
                        binding.spinnerSO.adapter = adapter

                    }
                }
            }
        }
    }

    private fun init() {
        if (sharePrefUtils.getString(Api.USER_TYPE).equals("ASM")) {
            viewModel.getTodaySummary(
                Calendar.getInstance().time.formatDate(),
                Calendar.getInstance().time.formatDate(),
                "0"
            )
        } else {
            getSOList()
        }
    }

    fun setDateFilter(urlSuffix: String) {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_WEEK, -7)
        val end = Calendar.getInstance().time
        val start = c.time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val simpleFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)
        val startDate = df.format(start)
        val endDate = df.format(end)

        binding.tvDateRange.text =
            getString(R.string.date_range_, simpleFormat.format(start), simpleFormat.format(end))
        sharePrefUtils.saveString(Api.START_DATE_ATTENDANCE, startDate)
        sharePrefUtils.saveString(Api.END_DATE_ATTENDANCE, endDate)

        getAttendance(Api.get_attendance + "?start_date=" + startDate + "&end_date=" + endDate + urlSuffix)

        val materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker()
        materialDateBuilder.setTheme(R.style.ThemeOverlay_App_MaterialCalendar)
        materialDateBuilder.setTitleText("SELECT A DATE")

        val materialDatePicker = materialDateBuilder.build()

        binding.dateRangeLayout.setOnClickListener {
            materialDatePicker.show(parentFragmentManager, "MATERIAL_DATE_PICKER")
            binding.dateRangeLayout.isEnabled = false
        }

        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            binding.dateRangeLayout.isEnabled = true
            val sDate = Date(selection.first!!)
            val eDate = Date(selection.second!!)
            if (sDate.compareTo(eDate) == 0) {
                binding.tvDateRange.text = simpleFormat.format(sDate)
                sharePrefUtils.saveString(Api.START_DATE_ATTENDANCE, df.format(sDate))
                sharePrefUtils.saveString(Api.END_DATE_ATTENDANCE, df.format(sDate))
            } else {
                binding.tvDateRange.text = getString(
                    R.string.date_range_,
                    simpleFormat.format(sDate),
                    simpleFormat.format(eDate)
                )

                sharePrefUtils.saveString(Api.START_DATE_ATTENDANCE, df.format(sDate))
                sharePrefUtils.saveString(Api.END_DATE_ATTENDANCE, df.format(eDate))
            }

            getAttendance(
                Api.get_attendance + "?start_date=" + df.format(sDate) + "&end_date=" + df.format(
                    eDate
                ) + urlSuffix
            )
        }

        materialDatePicker.addOnNegativeButtonClickListener {
            binding.dateRangeLayout.isEnabled = true
        }
    }

    private fun getAttendance(url: String) {
        ApiServices.apiGET(
            url,
            mQueue, sharePrefUtils.getString(Api.TOKEN)!!, object : ApiServiceListener {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onResponseSuccess(response: String) {
                    getHistoryList(response)
                }

                override fun onJSONResponseSuccess(response: JSONObject) {}

                override fun onNetworkResponseSuccess(response: NetworkResponse) {}

                override fun onResponseFailure(error: VolleyError) {
                    ViewUtils.getErrorResponse(error, requireContext())
                }

                override fun onException(e: Exception) {
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun viewSOList(response: String) {
        try {
            soList.clear()
            val obj = JSONObject(response)
            val toArray = obj.getJSONArray("so_list")
            val soArray = toArray.getJSONObject(0).getJSONArray("sales_officers")
            val soNameList: ArrayList<String> = ArrayList()
            soNameList.add(sharePrefUtils.getString(Api.NAME) + " (You)")

            if (soArray.length() > 0) {
                for (i in 0 until soArray.length()) {
                    val soObj = soArray.getJSONObject(i)
                    var imageUrl = "null"
                    if (soObj.has("images") && !soObj.isNull("images")) {
                        val imageArray = soObj.getJSONArray("images")
                        if (imageArray.length() > 0) {
                            val imageobj = imageArray.getJSONObject(0)
                            if (imageobj.has("image_url")) {
                                imageUrl = imageobj.getString("image_url")
                            }
                        }
                    }
                    soList.add(
                        SOList(
                            soObj.getString("id"),
                            soObj.getString("user_name"),
                            soObj.getString("designation"),
                            if (soObj.has("employee_id")) soObj.getString("employee_id") else "",
                            imageUrl
                        )
                    )
                    soNameList.add(soObj.getString("user_name"))

                }
            }
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item, soNameList
            )
            binding.spinnerSO.adapter = adapter
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getHistoryList(response: String) {
        try {
            val obj = JSONObject(response)
            val attedanceArray = obj.getJSONArray("attendances")
            historyList.clear()
            if (attedanceArray.length() > 0) {
                for (i in 0 until attedanceArray.length()) {
                    val attendanceObj = attedanceArray.getJSONObject(i)
                    var latitude = 0.0
                    var longitude = 0.0
                    var imageUrl = "null"
                    if (!attendanceObj.getString("checkin_time").equals("null")) {
                        if (!attendanceObj.getString("latitude").equals("null")) latitude =
                            attendanceObj.getDouble("latitude")
                        if (!attendanceObj.getString("longitude").equals("null")) longitude =
                            attendanceObj.getDouble("longitude")
                        if (attendanceObj.has("images") && !attendanceObj.isNull("images")) {
                            val imageArray = attendanceObj.getJSONArray("images")
                            if (imageArray.length() > 0) {
                                val imageobj = imageArray.getJSONObject(0)
                                if (imageobj.has("image_url")) {
                                    imageUrl = imageobj.getString("image_url")
                                }
                            }
                        }
                        historyList.add(
                            HistoryList(
                                attendanceObj.getString("user_name"),
                                attendanceObj.getString("user_id"),
                                attendanceObj.getString("id"),
                                attendanceObj.getString("checkin_time"),
                                attendanceObj.getString("checkout_time"),
                                attendanceObj.getInt("is_late"),
                                attendanceObj.getInt("is_absent"),
                                attendanceObj.getString("checkin_address"),
                                latitude,
                                longitude,
                                imageUrl,
                                attendanceObj.getString("remarks"),
                                attendanceObj.getString("route_id"),
                                attendanceObj.getString("route_name")
                            )
                        )
                    }
                }
            }

            if (selected_so == 0) {
                adapter = HistoryListTOAdapter(historyList)
                binding.historyTOListView.adapter = adapter
                adapter!!.notifyDataSetChanged()
            } else {
                filteredsoList.clear()
                filteredsoList.addAll(historyList)
                filteredsoList.removeIf {
                    !it.userId.equals(selected_so_id, true)
                }
                adapter = HistoryListTOAdapter(filteredsoList)
                binding.historyTOListView.adapter = adapter
                adapter!!.notifyDataSetChanged()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}