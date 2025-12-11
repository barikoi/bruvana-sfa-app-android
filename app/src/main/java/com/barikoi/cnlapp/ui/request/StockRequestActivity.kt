package com.barikoi.cnlapp.ui.request

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.ac.BaseActivity
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.data.remote.models.RequestStock
import com.barikoi.cnlapp.databinding.ActivityStockRequestBinding
import com.barikoi.cnlapp.ui.request.adapter.AdapterProductRead
import com.barikoi.cnlapp.ui.request.adapter.AdapterStockRequest
import com.barikoi.cnlapp.ui.request.vm.StockRequestViewModel
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.Constants
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.extension.setHapticClickListener
import com.barikoi.cnlapp.utils.extension.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class StockRequestActivity : BaseActivity() {
    private lateinit var binding: ActivityStockRequestBinding

    private val viewModel: StockRequestViewModel by viewModels()

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    private lateinit var adapter: AdapterStockRequest
    private lateinit var adapterProductRead: AdapterProductRead

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStockRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.tvTitle.text = getString(R.string.request)

        binding.toolbar.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        starRequestsObserve()


        adapter = AdapterStockRequest {
            requestDialog(it)
        }

        adapterProductRead = AdapterProductRead()

        binding.refresh.setOnRefreshListener {
            viewModel.getStockRequests("STOCK", sharePrefUtils.getString(Api.USER_ID)!!)
        }

        binding.rcvStockRequest.layoutManager = LinearLayoutManager(this)
        binding.rcvStockRequest.adapter = adapter

        binding.btnRetry.setHapticClickListener {
            viewModel.getStockRequests("STOCK", sharePrefUtils.getString(Api.USER_ID)!!)
        }


    }

    override fun onResume() {
        super.onResume()

        viewModel.getStockRequests("STOCK", sharePrefUtils.getString(Api.USER_ID)!!)

    }

    private fun requestDialog(request: RequestStock) {
        AppLogger.log("requestDialog:: $request")
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_product_stock_request)
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.setCancelable(true)
        dialog.window!!.attributes.windowAnimations = android.R.style.Animation_Dialog

        val tvRemarks = dialog.findViewById<TextView>(R.id.tvRemarks)
        if (request.remarks.isNullOrEmpty()) tvRemarks.text = "-- --" else tvRemarks.text =
            request.remarks
        val llStatus = dialog.findViewById<RelativeLayout>(R.id.llStatus)
        val tvStatus = dialog.findViewById<TextView>(R.id.tvStatus)

        dialog.findViewById<TextView>(R.id.tvSoName).text = request.requestFromUser.userName
        dialog.findViewById<TextView>(R.id.tvDBHouse).text = request.dbHouse?.dbHouseName ?: "-- --"
        dialog.findViewById<TextView>(R.id.tvApproveBy).text =
            if (request.respondedByUser == null) "-- --" else
                request.respondedByUser.userName + " at " + request.respondedAt


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
            }

            Constants.STATUS_PARTIAL_APPROVED -> {
                tvStatus.text = getString(R.string.partial_approved)

                tvStatus.background = AppCompatResources.getDrawable(
                    this,
                    R.drawable.shape_with_corner_green
                )
                llStatus.background = AppCompatResources.getDrawable(
                    this,
                    R.drawable.stroke_with_corner_green
                )
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
            }
        }

        val rcvProduct = dialog.findViewById<RecyclerView>(R.id.rcvProduct)
        rcvProduct.layoutManager = LinearLayoutManager(this)
        rcvProduct.adapter = adapterProductRead
        adapterProductRead.updateApproveData(request.stockProducts)

        dialog.show()
    }

    private fun starRequestsObserve() {
        lifecycleScope.launch {
            viewModel.requestsStockResponse.observe(this@StockRequestActivity) {
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
                        binding.llEmpty.isVisible = false

                        AppLogger.log("starStockRequestObserve::Loading")
                    }

                    is ApiState.Success -> {
                        binding.refresh.isRefreshing = false
                        binding.progressBar.isVisible = false
                        AppLogger.log("starStockRequestObserve:: Success ${it.data}")
                        if (it.data?.data != null && it.data.data.isEmpty()) {
                            binding.llEmpty.isVisible = true
                            return@observe
                        }
                        adapter.updateStockRequests(it.data?.data!!)
                    }
                }
            }
        }
    }
}