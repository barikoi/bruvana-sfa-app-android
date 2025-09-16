package com.barikoi.cnlapp.ui.order_delivery

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.data.remote.models.SoUser
import com.barikoi.cnlapp.data.remote.models.To
import com.barikoi.cnlapp.databinding.ActivityOrderDeliveryBinding
import com.barikoi.cnlapp.ui.adapter.ViewPagerAdapter
import com.barikoi.cnlapp.ui.order_delivery.vm.OrderDeliveryViewModel
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.Constants
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.extension.formatDate
import com.barikoi.cnlapp.utils.extension.formatDateWithDDMMYYYY
import com.barikoi.cnlapp.utils.extension.formatDateWithLocaleEnglish
import com.barikoi.cnlapp.utils.extension.formatFullMonthDateYear
import com.barikoi.cnlapp.utils.extension.getEndDateTime
import com.barikoi.cnlapp.utils.extension.getStartDateTime
import com.barikoi.cnlapp.utils.extension.setHapticClickListener
import com.barikoi.cnlapp.utils.extension.toast
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class OrderDeliveryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderDeliveryBinding
    private val viewModel: OrderDeliveryViewModel by viewModels()

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    var toList: List<To> = emptyList()
    var soListNew: List<SoUser> = emptyList()

    val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
        .setTitleText("Select Date Range")
        .build()

    var userIds: String? = null

    var startDate: String? = null
    var endDate: String? = null


    private lateinit var viewPagerAdapter: ViewPagerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOrderDeliveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.tvTitle.text = getString(R.string.order_delivery_update)

        binding.toolbar.btnBack.setHapticClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.editTextSearchShop.doOnTextChanged { text, _, _, _ ->
            viewModel.updateSearchQuery(text.toString())
        }

        if (sharePrefUtils.getString(Api.USER_TYPE).equals("ASM")) {
            viewModel.getTodaySummary(
                Calendar.getInstance().time.formatDate(),
                Calendar.getInstance().time.formatDate(),
                "0"
            )

        } else if (sharePrefUtils.getString(Api.USER_TYPE).equals("TO")) {
            binding.spinnerTO.isVisible = false

            viewModel.getSoList()

        } else if (sharePrefUtils.getString(Api.USER_TYPE).equals("SO")) {
            binding.spinnerLayoutTO.isVisible = false
            binding.spinnerLayoutSO.isVisible = false

        }

        startToObserve()
        startSOObserve()
        startSOListObserve()


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
                     userIds =
                        if (p2 == 0 && sharePrefUtils.getString(Api.USER_TYPE).equals("ASM")) {
                            soListNew.joinToString(",") { it.id.toString() }
                        } else {
                            soListNew[p2].id.toString()
                        }


                    callApi()
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        binding.tvDateRange.setHapticClickListener {
            dateRangePicker.show(supportFragmentManager, "date_range_picker")
        }

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDateMillis = selection.first
            val endDateMillis = selection.second

            startDate = Date(startDateMillis!!).formatDateWithLocaleEnglish()
            endDate = Date(endDateMillis!!).formatDateWithLocaleEnglish()

            viewModel.updateDateRange(
                startDate!!,
                endDate!!
            )

            binding.tvDateRange.text = getString(
                R.string.date_range_,
                Date(startDateMillis).formatFullMonthDateYear(),
                Date(endDateMillis).formatFullMonthDateYear()
            )

        }

        initDate()

        val titles = arrayOf(
            resources.getString(R.string.pending),
            resources.getString(R.string.delivered),
            resources.getString(R.string.bounced)
        )

        viewPagerAdapter = ViewPagerAdapter(
            supportFragmentManager, lifecycle, listOf(
                PendingFragment(),
                PendingFragment(),
                PendingFragment()
            )
        )

        binding.viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(
            binding.tabLayout, binding.viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            tab.text = titles[position]
        }.attach()

        for (i in 0 until binding.tabLayout.tabCount) {
            val tab = (binding.tabLayout.getChildAt(0) as ViewGroup).getChildAt(i)
            val p = tab.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(5, 15, 5, 15)
            tab.requestLayout()
        }

    }


    private fun initDate() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)

        startDate = cal.time.formatDateWithLocaleEnglish()
        endDate = cal.time.formatDateWithLocaleEnglish()

        viewModel.updateDateRange(
            startDate!!,
            endDate!!
        )

        binding.tvDateRange.text = startDate!!.formatDateWithDDMMYYYY()
    }

    private fun callApi() {
        if (sharePrefUtils.getString(Api.USER_TYPE).equals("ASM")) {
            viewModel.getSavedOrders(
                startDate!!.getStartDateTime(),
                endDate!!.getEndDateTime(),
                userIds!!,
                "PENDING",
                sharePrefUtils.getString(Constants.REGION_ID)!!,
                null
            )
        } else if (sharePrefUtils.getString(Api.USER_TYPE).equals("TO")) {
            viewModel.getSavedOrders(
                startDate!!.getStartDateTime(),
                endDate!!.getEndDateTime(),
                userIds!!,
                "PENDING",
                null,
                sharePrefUtils.getString(Constants.TERRITORY_ID)!!
            )
        } else {
            viewModel.getSavedOrders(
                startDate!!.getStartDateTime(),
                endDate!!.getEndDateTime(),
                userIds!!,
                "PENDING",
                null,
                null
            )
        }
    }

    private fun startToObserve() {
        lifecycleScope.launch {
            viewModel.toResponse.observe(this@OrderDeliveryActivity) {
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

                        toList = it.data!!.toList
                        val toNameList = it.data.toList.map { to -> to.toName }

                        val adapter = ArrayAdapter(
                            applicationContext,
                            android.R.layout.simple_spinner_item,
                            toNameList.toMutableList()
                        )
                        binding.spinnerTO.adapter = adapter

                    }
                }
            }
        }
    }

    private fun startSOObserve() {
        lifecycleScope.launch {
            viewModel.soResponse.observe(this@OrderDeliveryActivity) {
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

                        soListNew = it.data!!.users
                        val soNameList: MutableList<String> =
                            it.data.users.map { to -> to.userName }.toMutableList()

                        val adapter = ArrayAdapter(
                            applicationContext,
                            android.R.layout.simple_dropdown_item_1line,
                            soNameList.toMutableList()
                        )
                        binding.spinnerSO.adapter = adapter

                    }
                }
            }
        }
    }

    private fun startSOListObserve() {
        lifecycleScope.launch {
            viewModel.soListResponse.observe(this@OrderDeliveryActivity) {
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
                        if (it.data?.soList.isNullOrEmpty()) {
                            toast("So User is empty")
                            return@observe
                        }

                        soListNew = it.data!!.soList[0].salesOfficers.map { so ->
                            SoUser(
                                id = so.id,
                                userName = so.userName,
                                designation = so.designation,
                                phone = so.phone,
                                territoryId = so.territoryId
                            )
                        }
                        val soNameList: MutableList<String> =
                            it.data.soList[0].salesOfficers.map { to -> to.userName }
                                .toMutableList()

                        val adapter = ArrayAdapter(
                            applicationContext,
                            android.R.layout.simple_spinner_item,
                            soNameList.toMutableList()
                        )
                        binding.spinnerSO.adapter = adapter

                    }
                }
            }
        }
    }
}