package com.barikoi.cnlapp.ui.gift_summary

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.ac.BaseActivity
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.data.remote.models.GiftSummary
import com.barikoi.cnlapp.data.remote.models.SoUser
import com.barikoi.cnlapp.data.remote.models.To
import com.barikoi.cnlapp.databinding.ActivityGiftSumamryBinding
import com.barikoi.cnlapp.ui.gift_summary.adapter.AdapterGiftSummary
import com.barikoi.cnlapp.ui.gift_summary.vm.GiftSummaryViewModel
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.extension.formatDateWithLocale
import com.barikoi.cnlapp.utils.extension.getEndDateTime
import com.barikoi.cnlapp.utils.extension.getStartDateTime
import com.barikoi.cnlapp.utils.extension.loadingDialog
import com.barikoi.cnlapp.utils.extension.setHapticClickListener
import com.barikoi.cnlapp.utils.extension.toast
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlin.collections.indexOf

@AndroidEntryPoint
class GiftSummaryActivity : BaseActivity() {
    private lateinit var binding: ActivityGiftSumamryBinding

    private val viewModel: GiftSummaryViewModel by viewModels()

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    private lateinit var adapterGiftSummary: AdapterGiftSummary

    private lateinit var toDataDialog: Dialog

    private var formattedStartDate = ""
    private var formattedEndDate = ""

    val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
        .setTitleText("Select Date Range")
        .build()

    private var toList: List<To> = emptyList()
    private var soList: List<SoUser> = emptyList()
    private var selectedSO: String? = null
    private var isSorted = false

    private var gifts: List<GiftSummary> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGiftSumamryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog {
            toDataDialog = it
        }

        formattedStartDate = Date().formatDateWithLocale()
        formattedEndDate = Date().formatDateWithLocale()

        binding.tvDateRange.text =
            getString(
                R.string.date_range_,
                formattedStartDate.formatDateWithLocale(),
                formattedEndDate.formatDateWithLocale()
            )

        binding.ivFilter.setHapticClickListener {
            binding.ivFilter.setBackgroundResource(
                R.drawable.rounded_stroke_gift_selected
            )
            isSorted = !isSorted

            adapterGiftSummary.setGiftSummaryList(
                if (isSorted) gifts.sortedByDescending { it.total } else gifts.sortedBy { it.total }
            )
        }

        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
            viewModel.getGiftSummary(
                selectedSO ?: "",
                formattedStartDate.getStartDateTime(),
                formattedEndDate.getEndDateTime(),
            )
        }

        binding.llDateRange.setHapticClickListener {
            dateRangePicker.show(supportFragmentManager, "date_range_picker")
        }

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDateMillis = selection.first
            val endDateMillis = selection.second

            formattedStartDate = Date(startDateMillis!!).formatDateWithLocale()
            formattedEndDate = Date(endDateMillis!!).formatDateWithLocale()

            binding.tvDateRange.text =
                getString(
                    R.string.date_range_,
                    formattedStartDate.formatDateWithLocale(),
                    formattedEndDate.formatDateWithLocale()
                )

            viewModel.getGiftSummary(
                selectedSO!!,
                formattedStartDate.getStartDateTime(),
                formattedEndDate.getEndDateTime(),
            )
        }

        adapterGiftSummary = AdapterGiftSummary()
        binding.rcvGiftSummary.layoutManager = LinearLayoutManager(this)
        binding.rcvGiftSummary.adapter = adapterGiftSummary

        binding.toolbar.tvTitle.text = getString(R.string.title_gift_summary)
        binding.toolbar.btnBack.setHapticClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        startToObserve()
        startSoObserve()
        startGiftSummaryObserve()

        if (sharePrefUtils.getString(Api.USER_TYPE) == "ASM") {
            binding.llSpinnerTo.isVisible = true

            viewModel.getToList()

        } else if (sharePrefUtils.getString(Api.USER_TYPE) == "TO") {
            viewModel.getSoList(
                sharePrefUtils.getString(Api.USER_ID).toString()
            )
        } else {
            soList = listOf(
                SoUser(
                    "",
                    sharePrefUtils.getString(Api.USER_ID)!!.toInt(),
                    "",
                    0,
                    sharePrefUtils.getString(Api.NAME) ?: "No name",
                )
            )
            val adapter = ArrayAdapter(
                this@GiftSummaryActivity,
                android.R.layout.simple_spinner_dropdown_item,
                listOf(sharePrefUtils.getString(Api.NAME) ?: "No name")
            )
            binding.spinnerSO.adapter = adapter
        }

        binding.spinnerTO.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                p0: AdapterView<*>?,
                p1: View?,
                p2: Int,
                p3: Long
            ) {
                viewModel.getSoList(
                    toList[p2].toId.toString()
                )
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }


        binding.spinnerSO.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                p0: AdapterView<*>?,
                p1: View?,
                p2: Int,
                p3: Long
            ) {
                selectedSO = soList[p2].id.toString()
                viewModel.getGiftSummary(
                    selectedSO!!,
                    "$formattedStartDate 00:00:00",
                    "$formattedEndDate 23:59:59",
                )
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun startToObserve() {
        lifecycleScope.launch {
            viewModel.toResponse.observe(this@GiftSummaryActivity) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startLogoutObserve::Empty")

                        toDataDialog.show()
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startLogoutObserve::Error ${it.error}")
                        toDataDialog.dismiss()
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startLogoutObserve::Loading")
                        if (!toDataDialog.isShowing) {
                            toDataDialog.show()
                        }
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startLogoutObserve:: Success ${it.data}")
                        toDataDialog.dismiss()

                        if (it.data?.toList?.isEmpty() == true) {
                            toast("No route found")
                        } else {
                            toList = it.data!!.toList

                            val toNameList =
                                it.data.toList.map { to -> to.toName }.toMutableList()

                            val adapter = ArrayAdapter(
                                this@GiftSummaryActivity,
                                android.R.layout.simple_spinner_dropdown_item,
                                toNameList.toMutableList()
                            )
                            binding.spinnerTO.adapter = adapter
                            binding.spinnerTO.setSelection(
                                toNameList.indexOf(
                                    sharePrefUtils.getString(Api.SELECTED_ROUTE_NAME)
                                        ?: toNameList.firstOrNull()
                                )
                            )
                        }

                    }
                }
            }
        }
    }

    private fun startSoObserve() {
        lifecycleScope.launch {
            viewModel.soResponse.observe(this@GiftSummaryActivity) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startLogoutObserve::Empty")

                        toDataDialog.show()
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startLogoutObserve::Error ${it.error}")
                        toDataDialog.dismiss()
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startLogoutObserve::Loading")
                        if (!toDataDialog.isShowing) {
                            toDataDialog.show()
                        }
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startLogoutObserve:: Success ${it.data}")
                        toDataDialog.dismiss()

                        if (it.data?.users?.isEmpty() == true) {
                            toast("No route found")
                        } else {
                            soList = it.data!!.users

                            val soNameList =
                                it.data.users.map { to -> to.userName }.toMutableList()

                            val adapter = ArrayAdapter(
                                this@GiftSummaryActivity,
                                android.R.layout.simple_spinner_dropdown_item,
                                soNameList.toMutableList()
                            )
                            binding.spinnerSO.adapter = adapter
                        }

                    }
                }
            }
        }
    }

    private fun startGiftSummaryObserve() {
        lifecycleScope.launch {
            viewModel.giftSummaryResponse.observe(this@GiftSummaryActivity) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startGiftSummaryObserve::Empty")

                        toDataDialog.show()
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startGiftSummaryObserve::Error ${it.error}")
                        toDataDialog.dismiss()
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startGiftSummaryObserve::Loading")
                        if (!toDataDialog.isShowing) {
                            toDataDialog.show()
                        }
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startGiftSummaryObserve:: Success ${it.data}")
                        toDataDialog.dismiss()

                        gifts = it.data?.gifts ?: emptyList()

                        binding.tvTotalItem.text =
                            it.data?.gifts!!.sumOf { q -> q.total }.toString()

                        if (it.data.gifts.isEmpty()) {
                            toast("No gift found")
                            adapterGiftSummary.setGiftSummaryList(emptyList())
                            binding.includeEmpty.main.isVisible = true
                        } else {
                            binding.includeEmpty.main.isVisible = false
                            adapterGiftSummary.setGiftSummaryList(it.data.gifts)
                        }
                    }
                }
            }
        }
    }
}