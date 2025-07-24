package com.barikoi.cnlapp.Order_Delivery

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.Adapter.ViewPagerAdapter
import com.barikoi.cnlapp.Attendance.Model.SOList
import com.barikoi.cnlapp.Order_Delivery.Fragments.BouncedOrderFragment
import com.barikoi.cnlapp.Order_Delivery.Fragments.DeliveredOrderFragment
import com.barikoi.cnlapp.Order_Delivery.Fragments.PendingOrderFragment
import com.barikoi.cnlapp.Order_Delivery.vm.OrderDeliveryUpdateViewModel
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.ac.BaseActivity
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.data.remote.models.SoUser
import com.barikoi.cnlapp.data.remote.models.To
import com.barikoi.cnlapp.databinding.ActivityOrderDeliveryUpdateBinding
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.utils.ApiService.ApiServices
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.Constants
import com.barikoi.cnlapp.utils.RequestQueueSingleton
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.ViewUtils
import com.barikoi.cnlapp.utils.extension.formatDate
import com.barikoi.cnlapp.utils.extension.toast
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class OrderDeliveryUpdateActivity : BaseActivity() {
    private lateinit var binding: ActivityOrderDeliveryUpdateBinding

    private val viewModel: OrderDeliveryUpdateViewModel by viewModels()

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    var toList: List<To> = emptyList()
    var soListNew: List<SoUser> = emptyList()


    var token: String? = null

    var sr_id: String? = null
    var territory_id: String? = null
    var user_type: String? = null
    var route_id: String? = null
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var queue: RequestQueue? = null
    val soList: ArrayList<SOList> = ArrayList()

    companion object {
        var StartDate: String? = null
        var EndDate: String? = null

        @SuppressLint("StaticFieldLeak")
        var etSearchShop: AutoCompleteTextView? = null
        var selected_so: Int? = null
        var user_id: String? = null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOrderDeliveryUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startToObserve()
        startSOObserve()

        queue = RequestQueueSingleton.getInstance(applicationContext).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        editor = prefs!!.edit()
        token = prefs!!.getString(Api.TOKEN, "")
        user_type = prefs!!.getString(Api.USER_TYPE, "")
        territory_id = prefs!!.getString(Api.TERRITORY_ID, "")

        etSearchShop = findViewById(R.id.editTextSearchShop)

        if (user_type.equals("TO", true)) {
            sr_id = ""
            route_id = ""
            user_id = ""
            binding.spinnerLayoutSO.visibility = View.VISIBLE
            binding.spinnerLayoutTO.visibility = View.GONE
            getSOList()
        } else if (user_type.equals("ASM")) {
            sr_id = ""
            route_id = ""
            user_id = ""
            binding.spinnerLayoutTO.visibility = View.VISIBLE
            binding.spinnerLayoutSO.visibility = View.VISIBLE

            viewModel.getTodaySummary(
                Calendar.getInstance().time.formatDate(),
                Calendar.getInstance().time.formatDate(),
                "0"
            )

        } else {
            user_id = prefs!!.getString(Api.USER_ID, "")
            sr_id = prefs!!.getString(Api.EMPLOYEE_ID, "")
            route_id = prefs!!.getString(Api.SELECTED_ROUTE_ID, "")
            binding.spinnerLayoutSO.visibility = View.GONE
            binding.spinnerLayoutTO.visibility = View.GONE
            setDateFilter()
        }

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        binding.spinnerTO.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                viewModel.getSoByTo(toList[p2].toId.toString())
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        binding.spinnerSO.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (binding.spinnerSO.adapter.count > 0) {
                    user_id =
                        if (p2 == 0 && sharePrefUtils.getString(Api.USER_TYPE).equals("ASM")) {
                            soListNew.joinToString(",") { it.id.toString() }

                        } else {
                            soListNew[p2].id.toString()
                        }

                    setDateFilter()
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun startToObserve() {
        lifecycleScope.launch {
            viewModel.toResponse.observe(this@OrderDeliveryUpdateActivity) {
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

                        toList = it.data.toList
                        val toNameList = it.data.toList.map { to -> to.toName }

                        val adapter = ArrayAdapter(
                            applicationContext,
                            android.R.layout.simple_spinner_item, toNameList.toMutableList()
                        )
                        binding.spinnerTO.adapter = adapter

                    }
                }
            }
        }
    }

    private fun startSOObserve() {
        lifecycleScope.launch {
            viewModel.soResponse.observe(this@OrderDeliveryUpdateActivity) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startSOObserve::Empty")

                    }

                    is ApiState.Error -> {
                        AppLogger.log("startSOObserve::Error ${it.error}")

                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startSOObserve::Loading")

                    }

                    is ApiState.Success -> {
                        AppLogger.log("startSOObserve:: Success ${it.data}")
                        if (it.data?.users.isNullOrEmpty()) {
                            toast("So User is empty")
                            return@observe
                        }

                        soListNew = it.data.users
                        val soNameList: MutableList<String> =
                            it.data.users.map { to -> to.userName }.toMutableList()
                        soNameList.add(0, "All")

                        val adapter = ArrayAdapter(
                            applicationContext,
                            android.R.layout.simple_spinner_item, soNameList.toMutableList()
                        )
                        binding.spinnerSO.adapter = adapter

                    }
                }
            }
        }
    }

    private fun setTabLayoutView() {
        val titles = arrayOf(
            resources.getString(R.string.pending),
            resources.getString(R.string.delivered),
            resources.getString(R.string.bounced)
        )
        val fragments = ArrayList<Fragment>()
        fragments.add(PendingOrderFragment())
        fragments.add(DeliveredOrderFragment())
        fragments.add(BouncedOrderFragment())

        binding.viewPager.setAdapter(ViewPagerAdapter(supportFragmentManager, lifecycle, fragments))
        TabLayoutMediator(
            binding.viewpagertab, binding.viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            tab.text = titles[position]
        }.attach()

        binding.viewPager.setUserInputEnabled(false)
        for (i in 0 until binding.viewpagertab.tabCount) {
            val tab = (binding.viewpagertab.getChildAt(0) as ViewGroup).getChildAt(i)
            val p = tab.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(15, 15, 10, 15)
            tab.requestLayout()
        }
        Log.d("Fragment", "viewpager current Item: " + binding.viewPager.currentItem)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        binding.viewPager.currentItem = 0
                        PendingOrderFragment.checkForOrders(
                            queue!!,
                            token!!,
                            user_id!!,
                            sr_id!!,
                            territory_id!!,
                            sharePrefUtils.getString(Constants.REGION_ID)!!,
                            StartDate!!,
                            EndDate!!,
                            sharePrefUtils
                        )
                    }

                    1 -> {
                        binding.viewPager.currentItem = 1
                        DeliveredOrderFragment.checkforOrders(
                            queue!!,
                            token!!,
                            user_id!!,
                            sr_id!!,
                            territory_id!!,
                            sharePrefUtils.getString(Constants.REGION_ID)!!,
                            StartDate!!,
                            EndDate!!,
                            sharePrefUtils
                        )
                    }

                    2 -> {
                        binding.viewPager.currentItem = 2
                        BouncedOrderFragment.checkforOrders(
                            queue!!,
                            token!!,
                            user_id!!,
                            sr_id!!,
                            territory_id!!,
                            sharePrefUtils.getString(Constants.REGION_ID)!!,
                            StartDate!!,
                            EndDate!!,
                            sharePrefUtils
                        )
                    }
                }
            }
        })
    }

    private fun setDateFilter() {
        val c = Calendar.getInstance()
        c.add(Calendar.DATE, -1)
        val end = c.time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val simpleFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        StartDate = df.format(end)
        EndDate = df.format(end)

        binding.tvDateRange.text = simpleFormat.format(end)


        val materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker()
        materialDateBuilder.setTheme(R.style.ThemeOverlay_App_MaterialCalendar)
        materialDateBuilder.setTitleText(R.string.select_a_date)

        val materialDatePicker = materialDateBuilder.build()

        binding.dateRangeLayout.setOnClickListener {
            materialDatePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
            binding.dateRangeLayout.isEnabled = false
        }

        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            binding.dateRangeLayout.isEnabled = true
            val s_date = Date(selection.first!!)
            val e_date = Date(selection.second!!)
            if (s_date.compareTo(e_date) == 0) {
                binding.tvDateRange.text = simpleFormat.format(s_date)
                editor!!.putString(Api.START_DATE_ORDER, df.format(s_date))
                editor!!.putString(Api.END_DATE_ORDER, df.format(e_date))
                editor!!.commit()
            } else {
                binding.tvDateRange.text = getString(
                    R.string.date_range_,
                    simpleFormat.format(s_date),
                    simpleFormat.format(e_date)
                )
                editor!!.putString(Api.START_DATE_ORDER, df.format(s_date))
                editor!!.putString(Api.END_DATE_ORDER, df.format(e_date))
                editor!!.commit()
            }
            StartDate = df.format(s_date)
            EndDate = df.format(e_date)
            setTabLayoutView()

        }

        materialDatePicker.addOnNegativeButtonClickListener {
            binding.dateRangeLayout.isEnabled = true
        }
        setTabLayoutView()
    }

    private fun getSOList() {
        ApiServices.apiGET(
            Api.get_all_so_list,
            queue!!, token!!, object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    viewSOList(response)
                }

                override fun onJSONResponseSuccess(response: JSONObject) {}

                override fun onNetworkResponseSuccess(response: NetworkResponse) {}

                override fun onResponseFailure(error: VolleyError) {
                    ViewUtils.getErrorResponse(error, applicationContext)
                }

                override fun onException(e: Exception) {
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
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
            val soUsers: MutableList<SoUser> = mutableListOf()
            if (soArray.length() > 0) {
                for (i in 0 until soArray.length()) {
                    val soObj = soArray.getJSONObject(i)
                    val imageUrl = "null"
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

                    soUsers.add(
                        SoUser(
                            soObj.getString("designation"),
                            soObj.getString("id").toInt(),
                            "",
                            0,
                            soObj.getString("user_name"),
                        )
                    )

                }
            }

            soListNew = soUsers

            val adapter = ArrayAdapter(
                applicationContext,
                android.R.layout.simple_spinner_item, soNameList
            )
            binding.spinnerSO.adapter = adapter
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}