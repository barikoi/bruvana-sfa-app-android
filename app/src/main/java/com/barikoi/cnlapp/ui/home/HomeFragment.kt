package com.barikoi.cnlapp.ui.home

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
import com.barikoi.cnlapp.ui.summary_details.SummaryDetailsActivity
import com.barikoi.cnlapp.ui.to_details.TODetailsActivity
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.Constants
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.extension.formatDateWithDDMM
import com.barikoi.cnlapp.utils.extension.formatDateWithLocaleEnglish
import com.barikoi.cnlapp.utils.extension.getEndDateTime
import com.barikoi.cnlapp.utils.extension.getStartDateTime
import com.barikoi.cnlapp.utils.extension.setDebouncedClickListener
import com.barikoi.cnlapp.utils.extension.setHapticClickListener
import com.barikoi.cnlapp.utils.extension.toast
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment() {
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

    private lateinit var userType: String
    private var tId: String? = null
    private var userId: String? = null


    private var formattedStartDate = ""
    private var formattedEndDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            userType = it.getString(ARG_USER_TYPE)!!
            tId = it.getString(ARG_TID)
            userId = it.getString(ARG_USER_ID)
        }
    }

    companion object {
        private const val ARG_USER_TYPE = "arg_user_type"
        private const val ARG_TID = "arg_tid"
        private const val ARG_USER_ID = "arg_user_id"

        fun newInstance(
            userType: String,
            tId: String? = null,
            userId: String? = null
        ): HomeFragment {
            return HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_TYPE, userType)
                    putString(ARG_TID, tId)
                    putString(ARG_USER_ID, userId)
                }
            }
        }
    }

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

        adapterUserListWithSummary = AdapterUserListWithSummary { userSum, position ->
            if (userSum.userType == "SO") {
                startActivity(
                    Intent(requireContext(), SummaryDetailsActivity::class.java)
                        .putExtra("position", position)
                        .putParcelableArrayListExtra(
                            "user_summary",
                            ArrayList(userSummary)
                        )
                )
            } else {
                startActivity(
                    Intent(requireContext(), TODetailsActivity::class.java)
                        .putExtra("user_summary", userSum)
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

        val cal = Calendar.getInstance()
        formattedStartDate = cal.time.formatDateWithLocaleEnglish()
        formattedEndDate = cal.time.formatDateWithLocaleEnglish()

        binding.tvDateRange.text = formattedStartDate.formatDateWithDDMM()
        initData()

        binding.tvDateRange.setDebouncedClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireActivity(),
                { _, selectedYear, selectedMonth, selectedDay ->
//                    "yyyy-MM-dd"
                    formattedStartDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                    formattedEndDate = formattedStartDate

                    binding.tvDateRange.text = formattedStartDate.formatDateWithDDMM()


                    initData()
                },
                year, month, day
            )
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        binding.tvActiveTitle.setHapticClickListener {
            openActiveInactiveActivity(
                getString(R.string.active_user), active
            )
        }
        binding.tvActiveValue.setHapticClickListener {
            openActiveInactiveActivity(
                getString(R.string.active_user), active
            )
        }

        binding.tvInactiveTitle.setHapticClickListener {
            openActiveInactiveActivity(
                getString(R.string.inactive_user), inactive
            )
        }

        binding.tvInactiveValue.setHapticClickListener {
            openActiveInactiveActivity(
                getString(R.string.inactive_user), inactive
            )
        }

        startActiveInactiveUserObserve()
        startOverViewStatsObserve()
        startToWithTodaySummaryObserve()
        startSoWithTodaySummaryObserve()
    }

    private fun openActiveInactiveActivity(status: String, users: List<ActiveInactiveUser>) {
        startActivity(
            Intent(requireActivity(), ActiveInactiveActivity::class.java)
                .putExtra("so_status", status)
                .putParcelableArrayListExtra("users", ArrayList(users))
        )
    }

    private fun initData() {
        if (userType == "ASM") {
            viewModel.getActiveInactiveUsers(
                formattedStartDate, formattedEndDate,
                sharePrefUtils.getString(Api.USER_ID) ?: ""
            )

            viewModel.getOverViewStatsASM(
                formattedStartDate.getStartDateTime(), formattedEndDate.getEndDateTime(),
                sharePrefUtils.getString(Constants.REGION_ID) ?: "",
                sharePrefUtils.getString(Api.USER_ID) ?: ""
            )

            viewModel.getTOWIthSummary(
                formattedStartDate.getStartDateTime(), formattedEndDate.getEndDateTime(),
            )

        } else {
            viewModel.getActiveInactiveUsers(
                formattedStartDate, formattedEndDate,
                userId!!
            )
            viewModel.getOverViewStatsTO(
                formattedStartDate.getStartDateTime(), formattedEndDate.getEndDateTime(),
                tId ?: sharePrefUtils.getString(Constants.TERRITORY_ID) ?: "",
                userId!!
            )

            viewModel.getSOWIthSummary(
                formattedStartDate.getStartDateTime(), formattedEndDate.getEndDateTime(),
                userId!!
            )
        }
    }

    private fun startActiveInactiveUserObserve() {
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


    private fun startOverViewStatsObserve() {
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

    private fun startToWithTodaySummaryObserve() {
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

    private fun startSoWithTodaySummaryObserve() {
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