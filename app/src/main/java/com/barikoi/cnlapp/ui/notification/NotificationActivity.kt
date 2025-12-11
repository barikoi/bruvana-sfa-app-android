package com.barikoi.cnlapp.ui.notification

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.ac.BaseActivity
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.databinding.ActivityNotificationBinding
import com.barikoi.cnlapp.ui.approval.StockRequestApprovalActivity
import com.barikoi.cnlapp.ui.notification.vm.NotificationViewModel
import com.barikoi.cnlapp.ui.request.StockRequestActivity
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.Constants
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.extension.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class NotificationActivity : BaseActivity() {
    private lateinit var binding: ActivityNotificationBinding

    private val viewModel: NotificationViewModel by viewModels()

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    private lateinit var adapter: AdapterNotification
    private var requestType = Constants.OUTLET

    var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.getNotifications(
            sharePrefUtils.getString(Api.EMPLOYEE_ID)!!
        )

        startNotificationObserve()
        startReadNotificationObserve()

        binding.toolbar.tvTitle.text = getString(R.string.notifications)
        binding.toolbar.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.refresh.setOnRefreshListener {
            viewModel.getNotifications(
                sharePrefUtils.getString(Api.EMPLOYEE_ID)!!
            )
        }

        adapter = AdapterNotification {
            requestType = it.title
            if (it.readAt != null) {
                reDirect()
            } else {
                viewModel.readNotification(notificationId = it.id.toString())
            }
        }
        binding.rcvNotification.layoutManager = LinearLayoutManager(this)
        binding.rcvNotification.adapter = adapter
    }

    private fun startNotificationObserve() {
        lifecycleScope.launch {
            viewModel.notificationResponse.observe(this@NotificationActivity) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startNotificationObserve::Empty")
                        binding.progressBar.isVisible = false
                        binding.refresh.isRefreshing = false
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startNotificationObserve::Error ${it.error}")
                        binding.progressBar.isVisible = false
                        binding.refresh.isRefreshing = false
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startNotificationObserve::Loading")
                        binding.progressBar.isVisible = true
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startNotificationObserve:: Success ${it.data}")
                        binding.progressBar.isVisible = false
                        binding.refresh.isRefreshing = false

                        binding.llNotificationEmpty.isVisible =
                            it.data?.notifications.isNullOrEmpty()

                        it.data?.notifications?.let { it1 ->
                            adapter.updateNotifications(it1.sortedBy { s -> s.updatedAt }
                                .sortedBy { s -> s.readAt })
                        }
                    }
                }
            }
        }
    }

    private fun startReadNotificationObserve() {
        lifecycleScope.launch {
            viewModel.readNotificationResponse.observe(this@NotificationActivity) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startReadNotificationObserve::Empty")
                        binding.progressBar.isVisible = false
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startReadNotificationObserve::Error ${it.error}")
                        binding.progressBar.isVisible = false
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startReadNotificationObserve::Loading")
                        binding.progressBar.isVisible = true
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startReadNotificationObserve:: Success ${it.data}")
                        binding.progressBar.isVisible = false
                        viewModel.getNotifications(sharePrefUtils.getString(Api.EMPLOYEE_ID)!!)
                        reDirect()

//                        adapter.updateNotifications(it.data)
                    }
                }
            }
        }
    }

    private fun reDirect() {
        if (sharePrefUtils.getString(Api.USER_TYPE) == "TO") {
            startActivity(
                Intent(
                    this@NotificationActivity,
                    StockRequestApprovalActivity::class.java
                )
                    .putExtra(Constants.REQUEST_TYPE, requestType)
            )
            finish()
        } else {
            startActivity(
                Intent(
                    this@NotificationActivity,
                    StockRequestActivity::class.java
                )
                    .putExtra(Constants.REQUEST_TYPE, requestType)
            )
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.getNotifications(
            sharePrefUtils.getString(Api.EMPLOYEE_ID)!!
        )
    }
}