package com.barikoi.cnlapp.ui.product_summary

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.ac.BaseActivity
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.data.remote.models.DbHouse
import com.barikoi.cnlapp.data.remote.models.Product
import com.barikoi.cnlapp.databinding.ActivityProductSummaryBinding
import com.barikoi.cnlapp.ui.adapter.ProductStockAdapter
import com.barikoi.cnlapp.ui.product_summary.vm.ProductSummeryViewModel
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.Constants
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.extension.format
import com.barikoi.cnlapp.utils.extension.formatDate
import com.barikoi.cnlapp.utils.extension.formatDateWithDDmmYYYY
import com.barikoi.cnlapp.utils.extension.formatDateWithLocaleEnglish
import com.barikoi.cnlapp.utils.extension.formatFullMonthDateYear
import com.barikoi.cnlapp.utils.extension.getEndDateTime
import com.barikoi.cnlapp.utils.extension.getStartDateTime
import com.barikoi.cnlapp.utils.extension.toast
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ProductSummaryActivity : BaseActivity() {
    private lateinit var binding: ActivityProductSummaryBinding

    private val viewModel: ProductSummeryViewModel by viewModels()

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    private var products: List<Product> = emptyList()

    var isHighToLow = true

    var selectedTerritoryId: String? = null

    private lateinit var adapter: ProductStockAdapter
    private var dbHouses: List<DbHouse> = emptyList()


    val constraints = CalendarConstraints.Builder()
        .setValidator(DateValidatorPointBackward.now())
        .build()

    val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
        .setTitleText("Select Date Range")
        .setCalendarConstraints(constraints)
        .build()

    private var formattedStartDate = ""
    private var formattedEndDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProductSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.tvTitle.text = getString(R.string.title_product_summary)
        if (sharePrefUtils.getString(Api.USER_TYPE) == "SO") {
            binding.toolbar.tvSUbTitle.text =
                HtmlCompat.fromHtml(
                    "<b>DB House: </b> ${sharePrefUtils.getString(Constants.DB_HOUSE)}",
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            binding.toolbar.tvSUbTitle.isVisible = true

        }

        binding.toolbar.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        getDate()


        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDateMillis = selection.first
            val endDateMillis = selection.second

            formattedStartDate = Date(startDateMillis!!).formatDateWithLocaleEnglish()
            formattedEndDate = Date(endDateMillis!!).formatDateWithLocaleEnglish()

            binding.tvDateRange.text = getString(
                R.string.date_range_,
                formattedStartDate.formatDateWithDDmmYYYY(),
                formattedEndDate.formatDateWithDDmmYYYY()
            )

            apiCall()
        }

        binding.dateRangeLayout.setOnClickListener {
            dateRangePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
        }

        apiCall()


        starDHObserve()
        starProductStockObserve()

        adapter = ProductStockAdapter(
            true,
            sharePrefUtils.getString(Api.USER_TYPE) == "TO",
            {},
            {},
            {},
            { _, _ ->

            })

        binding.rcvProductList.layoutManager = LinearLayoutManager(this)
        binding.rcvProductList.adapter = adapter

        binding.spinnerDistributorHouse.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    if (binding.spinnerDistributorHouse.adapter.count > 0) {
                        selectedTerritoryId = dbHouses[p2].id.toString()

                        viewModel.getProductStock(
                            formattedStartDate.getStartDateTime(),
                            formattedEndDate.getEndDateTime(),
                            "1",
                            "1",
                            selectedTerritoryId,
                            null
                        )
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }

        if (sharePrefUtils.getString(Api.USER_TYPE).equals("TO", true)) {
            binding.spinnerLayoutRoute.visibility = View.VISIBLE
            viewModel.getDHList(sharePrefUtils.getString(Api.TERRITORY_ID)!!, null)
        } else if (sharePrefUtils.getString(Api.USER_TYPE).equals("ASM", true)) {
            binding.spinnerLayoutRoute.visibility = View.VISIBLE
            viewModel.getDHList(null, sharePrefUtils.getString(Constants.REGION_ID)!!)
        } else {
            binding.spinnerLayoutRoute.visibility = View.GONE
            viewModel.getProductStock(
                formattedStartDate.getStartDateTime(),
                formattedEndDate.getEndDateTime(),
                null,
                "1",
                null,
                sharePrefUtils.getString(Api.USER_ID)
            )
        }

        binding.llSort.setOnClickListener {
            val popup = PopupMenu(this@ProductSummaryActivity, binding.llSort)
            popup.menuInflater.inflate(R.menu.sort_menu_product_summary, popup.menu)
            popup.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener,
                PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    when (item.itemId) {
                        R.id.menu_high_to_low -> {
                            binding.sortTitle.text =
                                resources.getString(R.string.high_to_low)
                            adapter.updateProducts(
                                products.sortedByDescending { s ->
                                    s.productiveRoutes
                                }
                            )

                            isHighToLow = true

                        }

                        R.id.menu_low_to_high -> {
                            binding.sortTitle.text =
                                resources.getString(R.string.low_to_high)

                            adapter.updateProducts(
                                products.sortedBy { s ->
                                    s.productiveRoutes
                                }
                            )
                            isHighToLow = false
                        }
                    }
                    return true
                }
            })
            popup.show()
        }
    }

    private fun getDate() {
        val c = Calendar.getInstance()
        c.set(Calendar.DAY_OF_MONTH, 1)
        val end = Calendar.getInstance().time
        val start = c.time

        formattedStartDate = start.formatDateWithLocaleEnglish()
        formattedEndDate = end.formatDateWithLocaleEnglish()

        binding.tvDateRange.text = getString(
            R.string.date_range_,
            formattedStartDate.formatDateWithDDmmYYYY(),
            formattedEndDate.formatDateWithDDmmYYYY()
        )
    }

    private fun apiCall() {
        if (sharePrefUtils.getString(Api.USER_TYPE)
                .equals("TO", true) || sharePrefUtils.getString(Api.USER_TYPE)
                .equals("ASM", true)
        ) {
            viewModel.getProductStock(
                formattedStartDate.getStartDateTime(),
                formattedEndDate.getEndDateTime(),
                "1",
                "1",
                selectedTerritoryId,
                null
            )
        } else {
            viewModel.getProductStock(
                formattedStartDate.getStartDateTime(),
                formattedEndDate.getEndDateTime(),
                null,
                "1",
                null,
                sharePrefUtils.getString(Api.USER_ID)
            )
        }
    }

    private fun starDHObserve() {
        lifecycleScope.launch {
            viewModel.dbHousesResponse.observe(this@ProductSummaryActivity) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("starDHObserve::Empty")
                    }

                    is ApiState.Error -> {
                        AppLogger.log("starDHObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("starDHObserve::Loading")
                    }

                    is ApiState.Success -> {
                        AppLogger.log("starDHObserve:: Success ${it.data}")

                        if (it.data?.dbHouses == null) {
                            toast("DB House List is empty")
                            return@observe
                        }
                        dbHouses = it.data.dbHouses

                        val dhNameList = it.data.dbHouses.map { dh -> dh.dbHouseName }
                        val adapter = ArrayAdapter(
                            applicationContext,
                            android.R.layout.simple_spinner_dropdown_item, dhNameList
                        )
                        binding.spinnerDistributorHouse.adapter = adapter
                    }
                }
            }
        }
    }

    private fun starProductStockObserve() {
        lifecycleScope.launch {
            viewModel.productStockResponse.observe(this@ProductSummaryActivity) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("starProductStockObserve::Empty")
                        binding.progressBar.isVisible = false
                    }

                    is ApiState.Error -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("starProductStockObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        binding.progressBar.isVisible = true
                        AppLogger.log("starProductStockObserve::Loading")
                    }

                    is ApiState.Success -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("starProductStockObserve:: Success ${it.data}")

                        if (it.data?.products.isNullOrEmpty()) {
                            toast("Product List is empty")
                            binding.tvNoProducts.text =
                                getString(R.string.no_data_found)
                            return@observe
                        }

                        products = it.data?.products ?: emptyList()

                        if (isHighToLow) {
                            adapter.updateProducts(it.data!!.products!!.sortedByDescending { s ->
                                s.productiveRoutes
                            })
                        } else {
                            adapter.updateProducts(it.data!!.products!!.sortedBy { s ->
                                s.productiveRoutes
                            })
                        }


                        binding.tvTotalAmount.text =
                            getString(
                                R.string.total_amount,
                                "${it.data!!.products.sumOf { s -> s.deliveredAmount }}".format()
                            )
                    }
                }
            }
        }
    }
}