package com.barikoi.cnlapp.ui.gift_summary

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.data.remote.models.SoUser
import com.barikoi.cnlapp.data.remote.models.To
import com.barikoi.cnlapp.databinding.ActivityGiftSumamryBinding
import com.barikoi.cnlapp.ui.gift_summary.vm.GiftSummaryViewModel
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.extension.loadingDialog
import com.barikoi.cnlapp.utils.extension.setHapticClickListener
import com.barikoi.cnlapp.utils.extension.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.indexOf

@AndroidEntryPoint
class GiftSummaryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGiftSumamryBinding

    private val viewModel: GiftSummaryViewModel by viewModels()

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    private lateinit var toDataDialog: Dialog

    private var toList: List<To> = emptyList()
    private var soList: List<SoUser> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGiftSumamryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog {
            toDataDialog = it
        }

        binding.toolbar.tvTitle.text = getString(R.string.title_gift_summary)
        binding.toolbar.btnBack.setHapticClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        startToObserve()
        startSoObserve()

        if (sharePrefUtils.getString(Api.USER_TYPE) == "ASM") {
            binding.llSpinnerTo.isVisible = true

            viewModel.getToList()

        } else if (sharePrefUtils.getString(Api.USER_TYPE) == "TO") {
            viewModel.getSoList(
                sharePrefUtils.getString(Api.USER_ID).toString()
            )
        } else {
            val adapter = ArrayAdapter(
                this@GiftSummaryActivity,
                android.R.layout.simple_spinner_dropdown_item,
                listOf(sharePrefUtils.getString(Api.NAME) ?: "No name")
            )
            binding.spinnerSO.adapter = adapter
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
}