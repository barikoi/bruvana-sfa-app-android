package com.barikoi.cnlapp.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.StatisticsHome.Model.TargetAndCompleted
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.data.remote.models.ActiveInactiveUser
import com.barikoi.cnlapp.data.remote.models.UserSummary
import com.barikoi.cnlapp.databinding.FragmentHome2Binding
import com.barikoi.cnlapp.ui.active_inactive.ActiveInactiveActivity
import com.barikoi.cnlapp.ui.home.adapter.AdapterUserListWithSummary
import com.barikoi.cnlapp.ui.home.adapter.TargetAdapter
import com.barikoi.cnlapp.ui.home.vm.HomeViewModel
import com.barikoi.cnlapp.ui.summary_details.SummaryDetailsActivity2
import com.barikoi.cnlapp.ui.to_details.TODetailsActivity
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.Constants
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.extension.formatDateWithDDMM
import com.barikoi.cnlapp.utils.extension.formatDateWithLocale
import com.barikoi.cnlapp.utils.extension.setHapticClickListener
import com.barikoi.cnlapp.utils.extension.toast
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment(
    val userType: String,
    val tId: String? = null,
    val userId: String? = null,
) : Fragment() {
    private lateinit var binding: FragmentHome2Binding

    private val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    private lateinit var targetAdapter: TargetAdapter
    private lateinit var adapterUserListWithSummary: AdapterUserListWithSummary


    var active: List<ActiveInactiveUser> = emptyList()
    var userSummary: List<UserSummary> = emptyList()
    var inactive: List<ActiveInactiveUser> = emptyList()


    private var formattedStartDate = ""
    private var formattedEndDate = ""

    val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
        .setTitleText("Select Date Range")
        .build()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHome2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        targetAdapter = TargetAdapter()
        binding.rcvTarget.adapter = targetAdapter

        adapterUserListWithSummary = AdapterUserListWithSummary {
            if (it.userType == "SO") {
                startActivity(
                    Intent(requireContext(), SummaryDetailsActivity2::class.java)
                        .putParcelableArrayListExtra(
                            "user_summary",
                            ArrayList(userSummary)
                        )
                )
            } else {
                startActivity(
                    Intent(requireContext(), TODetailsActivity::class.java)
                        .putExtra("user_summary", it)
                )
            }
        }
        binding.rcvToList.layoutManager = LinearLayoutManager(requireContext())
        binding.rcvToList.adapter = adapterUserListWithSummary

        val emptyTargets = listOf(
            TargetAndCompleted(getString(R.string.total_target), "0.0", "0.0"),
            TargetAndCompleted(getString(R.string.ads), "0.0", "0.0"),
            TargetAndCompleted(getString(R.string.rds), "0.0", "0.0"),
            TargetAndCompleted(getString(R.string.sku_per_memo), "0.0", "0.0"),
            TargetAndCompleted(getString(R.string.number_of_memo), "0.0", "0.0"),
            TargetAndCompleted(getString(R.string.visit_ratio), "0.0", "0.0"),
            TargetAndCompleted(getString(R.string.aiv), "0.0", "0.0"),
            TargetAndCompleted(getString(R.string.bounce_p), "0.0", "0.0")
        )

        targetAdapter.updateData(emptyTargets)

        binding.refresh.setOnRefreshListener {
            binding.refresh.isRefreshing = false
            initData()
        }


        formattedEndDate = Date().formatDateWithLocale()
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        formattedStartDate = cal.time.formatDateWithLocale()

        binding.tvDateRange.text = getString(
            R.string.date_range_,
            formattedStartDate.formatDateWithDDMM(),
            formattedEndDate.formatDateWithDDMM()
        )

        viewModel.getActiveInactiveUsers(
            formattedStartDate, formattedEndDate
        )

        initData()

        binding.tvDateRange.setHapticClickListener {
            dateRangePicker.show(parentFragmentManager, "date_range_picker")
        }

        binding.tvActiveTitle.setHapticClickListener {
            startActivity(
                Intent(
                    requireActivity(), ActiveInactiveActivity::class.java
                ).putExtra(
                    "so_status",
                    getString(
                        R.string.active_user
                    )
                )
                    .putParcelableArrayListExtra(
                        "users",
                        ArrayList(active)
                    )
            )
        }

        binding.tvInactiveTitle.setHapticClickListener {
            startActivity(
                Intent(
                    requireActivity(), ActiveInactiveActivity::class.java
                )
                    .putExtra(
                        "so_status",
                        getString(
                            R.string.inactive_user
                        )
                    )
                    .putParcelableArrayListExtra(
                        "users",
                        ArrayList(inactive)
                    )

            )
        }

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDateMillis = selection.first
            val endDateMillis = selection.second

            formattedStartDate = Date(startDateMillis!!).formatDateWithLocale()
            formattedEndDate = Date(endDateMillis!!).formatDateWithLocale()

            sharePrefUtils.saveString(Api.START_DATE_ATTENDANCE, formattedStartDate)
            sharePrefUtils.saveString(Api.END_DATE_ATTENDANCE, formattedEndDate)

            binding.tvDateRange.text =
                getString(
                    R.string.date_range_,
                    formattedStartDate.formatDateWithDDMM(),
                    formattedEndDate.formatDateWithDDMM()
                )

            initData()
        }

        startActiveInactiveUserObserve()
        startOverViewStatsObserve()
        startToWithTodaySummaryObserve()
        startSoWithTodaySummaryObserve()
    }

    private fun initData() {
        if (userType == "ASM") {
            viewModel.getOverViewStatsASM(
                formattedStartDate, formattedEndDate,
                sharePrefUtils.getString(Constants.REGION_ID) ?: "",
                sharePrefUtils.getString(Api.USER_ID) ?: ""
            )

            viewModel.getTOWIthTodaySummary(
                formattedStartDate, formattedEndDate
            )

        } else {
            viewModel.getOverViewStatsTO(
                formattedStartDate, formattedEndDate,
                tId ?: sharePrefUtils.getString(Constants.TERRITORY_ID) ?: "",
                userId ?: sharePrefUtils.getString(Api.USER_ID) ?: ""
            )

            viewModel.getSOWIthTodaySummary(
                formattedStartDate,
                formattedEndDate,
                userId ?: sharePrefUtils.getString(Api.USER_ID) ?: ""
            )
        }

    }

    private fun startActiveInactiveUserObserve() {
        lifecycleScope.launch {
            viewModel.activeInactiveResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startActiveInactiveUserObserve::Empty")
                        binding.llActiveInactive.hideShimmer()
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startActiveInactiveUserObserve::Error ${it.error}")
                        binding.llActiveInactive.hideShimmer()

                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startActiveInactiveUserObserve::Loading")
                        binding.llActiveInactive.startShimmer()
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startActiveInactiveUserObserve:: Success ${it.data}")
                        binding.llActiveInactive.hideShimmer()

                        binding.tvActiveValue.text = it.data!!.active.size.toString()
                        binding.tvInactiveValue.text = it.data.inactive.size.toString()

                        active = it.data.active
                        inactive = it.data.inactive
                    }
                }
            }
        }
    }


    private fun startOverViewStatsObserve() {
        lifecycleScope.launch {
            viewModel.overViewStatsTOResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startOverViewStatsObserve::Empty")
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startOverViewStatsObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))

                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startOverViewStatsObserve::Loading")
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startOverViewStatsObserve:: Success ${it.data}")

                        val targets = listOf(
                            TargetAndCompleted(
                                resources.getString(R.string.total_target),
                                it.data!!.targets[0].targetAmount.toString(),
                                it.data.targetCompleted[0].revenue ?: "0"
                            ),
                            TargetAndCompleted(
                                resources.getString(R.string.ads),
                                it.data.targets[0].targetAds.toString(),
                                it.data.targetCompleted[0].ads.toString()
                            ),
                            TargetAndCompleted(
                                resources.getString(R.string.rds),
                                it.data.targets[0].targetRds ?: "0",
                                it.data.targetCompleted[0].rds.toString()
                            ),
                            TargetAndCompleted(
                                resources.getString(R.string.sku_per_memo),
                                it.data.targets[0].targetSkuPerMemo.toString(),
                                it.data.targetCompleted[0].skuPerMemo ?: "0"
                            ),
                            TargetAndCompleted(
                                resources.getString(R.string.number_of_memo),
                                it.data.targets[0].targetNumberOfMemo.toString(),
                                it.data.targetCompleted[0].numberOfMemo.toString()
                            ),
                            TargetAndCompleted(
                                resources.getString(R.string.visit_ratio),
                                it.data.targets[0].targetNumberOfVisits.toString(),
                                it.data.targetCompleted[0].numberOfVisits.toString()
                            ),
                            TargetAndCompleted(
                                resources.getString(R.string.aiv),
                                it.data.targets[0].targetAiv.toString(),
                                it.data.targetCompleted[0].aiv ?: "0"
                            ),
                            TargetAndCompleted(
                                resources.getString(R.string.bounce_p),
                                it.data.targets[0].thresholdBouncePercentage.toString(),
                                it.data.targetCompleted[0].bounceAmountPercentage.toString()
                            )
                        )

                        targetAdapter.updateData(targets)
                    }
                }
            }
        }
    }

    private fun startToWithTodaySummaryObserve() {
        lifecycleScope.launch {
            viewModel.toWithTodaySummaryResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startToWithTodaySummaryObserve::Empty")
                        binding.llShimmerToList.isVisible = false
                        binding.llShimmerToList.hideShimmer()
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startToWithTodaySummaryObserve::Error ${it.error}")
                        binding.llShimmerToList.isVisible = false
                        binding.llShimmerToList.hideShimmer()
                        toast(networkFailureMessage.handleFailure(it.error!!))

                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startToWithTodaySummaryObserve::Loading")
                        binding.llShimmerToList.isVisible = true
                        binding.rcvToList.isVisible = false
                        binding.llShimmerToList.startShimmer()
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startToWithTodaySummaryObserve:: Success ${it.data}")
                        binding.llShimmerToList.isVisible = false
                        binding.llShimmerToList.hideShimmer()
                        binding.rcvToList.isVisible = true

                        userSummary =
                            it.data!!.toList.map { tow ->
                                tow.toUserSummary()
                            }
                        adapterUserListWithSummary.updateData(userSummary)
                    }
                }
            }
        }
    }

    private fun startSoWithTodaySummaryObserve() {
        lifecycleScope.launch {
            viewModel.soWithTodaySummaryResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startSoWithTodaySummaryObserve::Empty")
                        binding.llShimmerToList.isVisible = false
                        binding.llShimmerToList.hideShimmer()
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startSoWithTodaySummaryObserve::Error ${it.error}")
                        binding.llShimmerToList.isVisible = false
                        binding.llShimmerToList.hideShimmer()
                        toast(networkFailureMessage.handleFailure(it.error!!))

                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startSoWithTodaySummaryObserve::Loading")
                        binding.llShimmerToList.isVisible = true
                        binding.rcvToList.isVisible = false
                        binding.llShimmerToList.startShimmer()
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startSoWithTodaySummaryObserve:: Success ${it.data}")
                        binding.llShimmerToList.isVisible = false
                        binding.rcvToList.isVisible = true
                        binding.llShimmerToList.hideShimmer()


                        userSummary = it.data!!.soList[0].salesOfficers.map { so ->
                            so.toUserSummary()
                        }

                        adapterUserListWithSummary.updateData(userSummary)
                    }
                }
            }
        }
    }
}