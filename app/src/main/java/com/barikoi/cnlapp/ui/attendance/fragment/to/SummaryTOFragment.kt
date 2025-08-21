package com.barikoi.cnlapp.ui.attendance.fragment.to

import android.annotation.SuppressLint
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
import coil3.load
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.data.remote.models.To
import com.barikoi.cnlapp.databinding.FragmentSummaryTOBinding
import com.barikoi.cnlapp.ui.attendance.adapter.so.ReasonListAdapter
import com.barikoi.cnlapp.ui.attendance.model.SOList
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
class SummaryTOFragment : Fragment() {
    private lateinit var binding: FragmentSummaryTOBinding

    private val viewModel: SummaryTOViewModel by viewModels()

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage


    @Inject
    lateinit var mQueue: RequestQueue


    private var toList: List<To> = emptyList()

    var adapter: ReasonListAdapter? = null

    var selectedSO: Int? = null
    var selectedSOId: String? = null
    val soList: ArrayList<SOList> = ArrayList()
    val filteredSOList: ArrayList<Pair<String, String>> = ArrayList()
    private var reasonList: ArrayList<Pair<String, String>> = ArrayList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

        startToObserve()

        binding.spinnerSO.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (binding.spinnerSO.adapter.count > 0) {
                    if (sharePrefUtils.getString(Api.USER_TYPE).equals("ASM")) {
                        selectedSO = p2
                        if (p2 > 0) {
                            binding.userLayout.visibility = View.VISIBLE
                            selectedSOId = toList[p2 - 1].toId.toString()
                            binding.imageUser.visibility = View.GONE

                            binding.userName.text = toList[p2 - 1].toName
                            binding.userDesignation.text = "TO"
                        } else {
                            binding.userLayout.visibility = View.GONE
                        }
                        if (p2 == 0) {
                            setDateFilter("&with_asm=1")
                        } else {
                            setDateFilter("")
                        }
                    } else {
                        selectedSO = p2
                        if (p2 > 0) {
                            binding.userLayout.visibility = View.VISIBLE
                            selectedSOId = soList[p2 - 1].id
                            if (soList[p2 - 1].imageUrl != "null") {
                                binding.imageUser.load(
                                    soList[p2 - 1].imageUrl
                                )
                            } else {
                                binding.imageUser.visibility = View.GONE
                            }
                            binding.userName.text = soList[p2 - 1].name
                            binding.userDesignation.text = soList[p2 - 1].designation
                        } else {
                            binding.userLayout.visibility = View.GONE
                        }
                        if (p2 == 0) {
                            setDateFilter("&with_to=1")
                        } else {
                            setDateFilter("")
                        }
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSummaryTOBinding.inflate(inflater, container, false)
        return binding.root
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
            binding.dateRangeLayout.isEnabled =
                true
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

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    fun getHistoryList(response: String) {
        try {
            val obj = JSONObject(response)
            val attedanceArray = obj.getJSONArray("attendances")
            var absent = 0
            var present = 0
            var late = 0
            var total = 0
            reasonList.clear()
            if (attedanceArray.length() > 0) {
                for (i in 0 until attedanceArray.length()) {
                    val attendanceObj = attedanceArray.getJSONObject(i)
                    if (attendanceObj.getString("user_id").equals(selectedSOId)) {
                        total += 1
                        if (!attendanceObj.getString("remarks")
                                .equals("null") && attendanceObj.getString("remarks").isNotEmpty()
                        ) {
                            reasonList.add(
                                Pair(
                                    attendanceObj.getString("checkin_time"),
                                    attendanceObj.getString("remarks")
                                )
                            )
                        }
                        if (attendanceObj.getString("checkin_time")
                                .equals("null") && attendanceObj.getInt("is_absent") == 0
                        ) {
                            total -= 1
                        }
                        if (attendanceObj.getInt("is_late") == 1) late += 1
                        if (attendanceObj.getInt("is_absent") == 1) absent += 1
                    } else {
                        if (selectedSO == 0) {
                            total += 1
                            if (!attendanceObj.getString("remarks")
                                    .equals("null") && attendanceObj.getString("remarks")
                                    .isNotEmpty()
                            ) {
                                reasonList.add(
                                    Pair(
                                        attendanceObj.getString("checkin_time"),
                                        attendanceObj.getString("remarks")
                                    )
                                )
                            }
                            if (attendanceObj.getString("checkin_time")
                                    .equals("null") && attendanceObj.getInt("is_absent") == 0
                            ) {
                                total -= 1
                            }
                            if (attendanceObj.getInt("is_late") == 1) late += 1
                            if (attendanceObj.getInt("is_absent") == 1) absent += 1
                        }
                    }
                }

                present = total - absent

                sharePrefUtils.saveInt(Api.TOTAL_PRESENT, present)
                sharePrefUtils.saveInt(Api.TOTAL_LATE, late)
                sharePrefUtils.saveInt(Api.TOTAL_ABSENT, absent)
            }

            val adapter = ReasonListAdapter(reasonList)
            binding.summaryListTOView.adapter = adapter
            adapter.notifyDataSetChanged()

            binding.presentCount.text = present.toString()
            binding.lateCount.text = late.toString()
            binding.absentCount.text = absent.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

}