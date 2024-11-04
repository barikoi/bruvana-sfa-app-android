package com.barikoi.cnlapp.approval

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.approval.adapter.AdapterApproval
import com.barikoi.cnlapp.approval.adapter.AdapterApprove
import com.barikoi.cnlapp.approval.adapter.StockRequest
import com.barikoi.cnlapp.base.ac.BaseActivity
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.data.remote.models.RequestStock
import com.barikoi.cnlapp.data.remote.models.request.NewStockData
import com.barikoi.cnlapp.data.remote.models.request.StockApprovalRequest
import com.barikoi.cnlapp.databinding.ActivityStockRequestApprovalBinding
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.Constants
import com.barikoi.cnlapp.utils.Constants.APPROVED
import com.barikoi.cnlapp.utils.Constants.PARTIAL_APPROVED
import com.barikoi.cnlapp.utils.Constants.REJECTED
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
class StockRequestApprovalActivity : BaseActivity() {
    private lateinit var binding: ActivityStockRequestApprovalBinding
    private val viewModel: StockRequestApprovalViewModel by viewModels()

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils


    private lateinit var adapter: AdapterApproval

    private lateinit var adapterApprove: AdapterApprove


    private var original = emptyList<StockRequest>()
    private var tmp = emptyList<StockRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStockRequestApprovalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.tvTitle.text = getString(R.string.title_approval)
        binding.toolbar.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        starRequestsObserve()
        starUpdateRequestObserve()

        getDate()
        setDateFilter()

        adapter = AdapterApproval(Constants.STOCK) {
            AppLogger.log("adapter:: $it")
            tmp = it.stockProducts.map { data ->
                StockRequest(
                    data.id,
                    data.dbHouseId,
                    data.productName ?: getString(R.string.das_das),
                    data.currentAvailableStock,
                    true
                )
            }
            original = tmp.map { c -> c.copy() }

            approvalDialog(it)
        }

        binding.rcvStockRequestApproval.layoutManager = LinearLayoutManager(this)
        binding.rcvStockRequestApproval.adapter = adapter

        binding.refresh.setOnRefreshListener {
            viewModel.getRequestsStock(Constants.STOCK, sharePrefUtils.getString(Api.USER_ID)!!)
        }

    }

    override fun onResume() {
        super.onResume()

        viewModel.getRequestsStock(Constants.STOCK, sharePrefUtils.getString(Api.USER_ID)!!)

    }

    private fun approvalDialog(request: RequestStock) {
        AppLogger.log("approvalDialog:: $request")
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_product_stock_approval)
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)
        dialog.window!!.attributes.windowAnimations = android.R.style.Animation_Dialog

        val etxRemarks = dialog.findViewById<EditText>(R.id.etRemark)
        val llStatus = dialog.findViewById<RelativeLayout>(R.id.llStatus)
        val tvStatus = dialog.findViewById<TextView>(R.id.tvStatus)
        val tvRemarks = dialog.findViewById<TextView>(R.id.tvRemarks)
        val tvApproveBy = dialog.findViewById<TextView>(R.id.tvApproveBy)

        if (request.remarks.isNullOrEmpty()) {
            tvRemarks.text = getString(R.string.das_das)
        } else {
            tvRemarks.text = request.remarks
        }

        if (request.respondedByUser == null) {
            tvApproveBy.text = getString(R.string.das_das)
        } else {
            tvApproveBy.text =
                getString(R.string.at, request.respondedByUser.userName, request.respondedAt)
        }

        val groupApprove = dialog.findViewById<Group>(R.id.groupApprove)
        val groupRead = dialog.findViewById<Group>(R.id.groupRead)
        val tvSelectAll = dialog.findViewById<TextView>(R.id.tvSelectAll)
        val tvClear = dialog.findViewById<TextView>(R.id.tvClear)

        dialog.findViewById<TextView>(R.id.tvSoName).text = request.requestFromUser.userName
        dialog.findViewById<TextView>(R.id.tvDBHouse).text =
            request.dbHouse?.dbHouseName ?: getString(R.string.das_das)


        when (request.status) {
            Constants.STATUS_PENDING -> {
                tvStatus.text = getString(R.string.pending)

                tvStatus.background = AppCompatResources.getDrawable(
                    this,
                    R.drawable.shape_with_corner_yellow
                )
                llStatus.background = AppCompatResources.getDrawable(
                    this,
                    R.drawable.stroke_with_corner_yellow
                )

                groupApprove.isVisible = true
                groupRead.isVisible = false
                tvSelectAll.isVisible = true
                tvClear.isVisible = true
            }

            Constants.STATUS_APPROVED -> {
                tvStatus.text = getString(R.string.approved)

                tvStatus.background = AppCompatResources.getDrawable(
                    this,
                    R.drawable.shape_with_corner_green
                )
                llStatus.background = AppCompatResources.getDrawable(
                    this,
                    R.drawable.stroke_with_corner_green
                )

                groupApprove.isVisible = false
                groupRead.isVisible = true
                tvSelectAll.isVisible = false
                tvClear.isVisible = false
            }

            Constants.STATUS_PARTIAL_APPROVED -> {
                tvStatus.text = getString(R.string.approved)

                tvStatus.background = AppCompatResources.getDrawable(
                    this,
                    R.drawable.shape_with_corner_green
                )
                llStatus.background = AppCompatResources.getDrawable(
                    this,
                    R.drawable.stroke_with_corner_green
                )

                groupApprove.isVisible = false
                groupRead.isVisible = true
                tvSelectAll.isVisible = false
                tvClear.isVisible = false
            }

            Constants.STATUS_DECLINED -> {
                tvStatus.text = getString(R.string.declined)
                tvStatus.background = AppCompatResources.getDrawable(
                    this,
                    R.drawable.shape_with_corner_red
                )
                llStatus.background = AppCompatResources.getDrawable(
                    this,
                    R.drawable.stroke_with_corner_red
                )

                groupApprove.isVisible = false
                groupRead.isVisible = true
                tvSelectAll.isVisible = false
                tvClear.isVisible = false

            }
        }

        dialog.findViewById<View>(R.id.btnDecline).setOnClickListener {

            val data = tmp.map { stock ->
                NewStockData(
                    stock.stock,
                    stock.dbHouseId.toString(),
                    stock.id,
                    2,
                    stock.stock,
                    stock.name
                )
            }
            val allData = StockApprovalRequest(data, etxRemarks.text.toString(), REJECTED)

            viewModel.updateRequest(request.id.toString(), allData)
            dialog.dismiss()
        }

        dialog.findViewById<View>(R.id.btnApprove).setOnClickListener {
            dialog.dismiss()
            AppLogger.log("tmp:: $tmp Size:: ${tmp.size}")
            AppLogger.log("original:: $original Size:: ${original.size}")

            val isAnyUnSelected = tmp.any { !it.isSelect }
            var count = 0
            tmp.mapIndexed { index, stockRequest ->
                if (original[index].stock != stockRequest.stock) {
                    count++
                }
            }

            AppLogger.log("stockChanges:: $count")
            AppLogger.log("isAnyUnSelected:: $isAnyUnSelected")
            AppLogger.log("isAnyUnSelected:: $isAnyUnSelected")


            val data = tmp.mapIndexed { pos, stock ->
                if (!stock.isSelect) {
                    NewStockData(
                        stock.stock,
                        stock.dbHouseId.toString(),
                        stock.id,
                        2,
                        stock.stock,
                        stock.name
                    )
                } else {
                    if (stock.stock != original[pos].stock) {
                        NewStockData(
                            original[pos].stock,
                            stock.dbHouseId.toString(),
                            stock.id,
                            3,
                            stock.stock,
                            stock.name
                        )
                    } else {
                        NewStockData(
                            stock.stock,
                            stock.dbHouseId.toString(),
                            stock.id,
                            1,
                            original[pos].stock,
                            stock.name
                        )
                    }
                }

            }

            AppLogger.log("PROCESS DATA:: $data")

            if (isAnyUnSelected || count > 0) {
                viewModel.updateRequest(
                    request.id.toString(),
                    StockApprovalRequest(data, etxRemarks.text.toString(), PARTIAL_APPROVED)
                )
            } else {
                viewModel.updateRequest(
                    request.id.toString(),
                    StockApprovalRequest(data, etxRemarks.text.toString(), APPROVED)
                )
            }

        }


        tvSelectAll.setOnClickListener {
            tmp.forEach {
                it.isSelect = true
            }
            adapterApprove.updateApproveData(tmp)
        }

        tvClear.setOnClickListener {
            tmp.forEach {
                it.isSelect = false
            }
            adapterApprove.updateApproveData(tmp)
        }

        adapterApprove = AdapterApprove({
            tmp[it].isSelect = !tmp[it].isSelect

            adapterApprove.updateApproveData(tmp)
        }, { text, position ->
            tmp[position].stock = text.toInt()
        })

        val rcvProduct = dialog.findViewById<RecyclerView>(R.id.rcvProduct)
        rcvProduct.layoutManager = LinearLayoutManager(this)
        rcvProduct.adapter = adapterApprove

        adapterApprove.updateApproveData(tmp)

        dialog.show()
    }

    private fun starRequestsObserve() {
        lifecycleScope.launch {
            viewModel.requestsStockResponse.observe(this@StockRequestApprovalActivity) {
                when (it) {
                    is ApiState.Empty -> {
                        binding.refresh.isRefreshing = false
                        binding.progressBar.isVisible = false
                        AppLogger.log("starStockRequestObserve::Empty")
                    }

                    is ApiState.Error -> {
                        binding.refresh.isRefreshing = false
                        binding.progressBar.isVisible = false
                        AppLogger.log("starStockRequestObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        binding.progressBar.isVisible = true
                        AppLogger.log("starStockRequestObserve::Loading")
                    }

                    is ApiState.Success -> {
                        binding.refresh.isRefreshing = false
                        binding.progressBar.isVisible = false
                        AppLogger.log("starStockRequestObserve:: Success ${it.data}")
                        adapter.updateShopRequests(it.data?.data!!.sortedByDescending { s -> s.status == Constants.STATUS_PENDING })
                    }
                }
            }
        }
    }

    private fun starUpdateRequestObserve() {
        lifecycleScope.launch {
            viewModel.updateStockRequestResponse.observe(this@StockRequestApprovalActivity) {
                when (it) {
                    is ApiState.Empty -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("starStockRequestObserve::Empty")
                    }

                    is ApiState.Error -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("starStockRequestObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        binding.progressBar.isVisible = true
                        AppLogger.log("starStockRequestObserve::Loading")
                    }

                    is ApiState.Success -> {
                        binding.progressBar.isVisible = false
                        binding.refresh.isRefreshing = false
                        AppLogger.log("starStockRequestObserve:: Success ${it.data}")
                        toast(it.data!!.message)

                        viewModel.getRequestsStock(
                            Constants.STOCK,
                            sharePrefUtils.getString(Api.USER_ID)!!
                        )
                    }
                }
            }
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
            binding.dateRangeLayout.setEnabled(false)
        }

        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            binding.dateRangeLayout.setEnabled(true)
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

//            viewModel.getProductStock(
//                df.format(sDate) + " 00:00:00",
//                df.format(eDate) + " 23:59:59",
//                null,
//                "1",
//                null,
//                selectedTerritoryId
//            )

        }

        materialDatePicker.addOnNegativeButtonClickListener {
            binding.dateRangeLayout.setEnabled(
                true
            )
        }
    }
}