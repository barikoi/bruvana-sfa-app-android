package com.barikoi.cnlapp.ProductStock

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barikoi.cnlapp.ProductStock.vm.ProductSummeryViewModel
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.ac.BaseActivity
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.data.remote.models.DbHouse
import com.barikoi.cnlapp.databinding.ActivityProductSummaryBinding
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.Api.TERRITORY_ID
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.Constants
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.extension.toast
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

    var selectedTerritoryId: String? = null

    private lateinit var adapter: ProductStockAdapter
    private var dbHouses: List<DbHouse> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProductSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.tvTitle.text = getString(R.string.title_product_summary)
        binding.toolbar.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        setDateFilter()

        starDHObserve()
        starProductStockObserve()

        adapter = ProductStockAdapter(true,
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
                            "${getDate().first} 00:00:00",
                            "${getDate().second} 23:59:59",
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
            viewModel.getDHList(sharePrefUtils.getString(TERRITORY_ID)!!, null)
        } else if (sharePrefUtils.getString(Api.USER_TYPE).equals("ASM", true)) {
            binding.spinnerLayoutRoute.visibility = View.VISIBLE
            viewModel.getDHList(null, sharePrefUtils.getString(Constants.REGION_ID)!!)
        } else {
            binding.spinnerLayoutRoute.visibility = View.GONE
            viewModel.getProductStock(
                "${getDate().first} 00:00:00",
                "${getDate().second} 23:59:59",
                null,
                "1",
                null,
                sharePrefUtils.getString(Api.USER_ID)
            )
        }
    }

    private fun getDate(): Pair<String, String> {
        val c = Calendar.getInstance()
        c.set(Calendar.DAY_OF_MONTH, 1)
        val end = Calendar.getInstance().time
        val start = c.time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val simpleFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

        binding.tvDateRange.text =
            getString(R.string.date_range_, simpleFormat.format(start), simpleFormat.format(end))

        return Pair(df.format(start), df.format(end))
    }


    private fun setDateFilter() {
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val simpleFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

        val materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker()
        materialDateBuilder.setTheme(R.style.ThemeOverlay_App_MaterialCalendar)
        materialDateBuilder.setTitleText(getString(R.string.select_a_date))

        val materialDatePicker = materialDateBuilder.build()

        binding.dateRangeLayout.setOnClickListener {
            materialDatePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
            binding.dateRangeLayout.isEnabled = false
        }

        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            binding.dateRangeLayout.isEnabled = true
            val sDate = Date(selection.first!!)
            val eDate = Date(selection.second!!)
            if (sDate.compareTo(eDate) == 0) {
                binding.tvDateRange.text = simpleFormat.format(sDate)
            } else {
                binding.tvDateRange.text = getString(
                    R.string.date_range_,
                    simpleFormat.format(sDate),
                    simpleFormat.format(eDate)
                )
            }

            viewModel.getProductStock(
                df.format(sDate) + " 00:00:00",
                df.format(eDate) + " 23:59:59",
                null,
                "1",
                null,
                selectedTerritoryId
            )

        }

        materialDatePicker.addOnNegativeButtonClickListener {
            binding.dateRangeLayout.isEnabled = true
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
                            android.R.layout.simple_spinner_item, dhNameList
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


                        adapter.updateProducts(it.data?.products!!)
                    }
                }
            }
        }
    }
}