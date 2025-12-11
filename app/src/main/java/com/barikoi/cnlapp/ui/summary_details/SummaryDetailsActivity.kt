package com.barikoi.cnlapp.ui.summary_details

import android.app.Dialog
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.ac.BaseActivity
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.data.remote.models.UserSummary
import com.barikoi.cnlapp.databinding.ActivitySummaryDetails2Binding
import com.barikoi.cnlapp.ui.adapter.ViewPagerAdapter
import com.barikoi.cnlapp.ui.summary_details.fragment.outlet.OutletTypeSummaryFragment
import com.barikoi.cnlapp.ui.summary_details.fragment.so.SoDetailsSummaryFragment
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.Constants
import com.barikoi.cnlapp.utils.NotificationUtils
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.extension.formatDateWithDDmmYYYY
import com.barikoi.cnlapp.utils.extension.formatDateWithLocaleEnglish
import com.barikoi.cnlapp.utils.extension.getEndDateTime
import com.barikoi.cnlapp.utils.extension.getParcelableArrayListCompat
import com.barikoi.cnlapp.utils.extension.getStartDateTime
import com.barikoi.cnlapp.utils.extension.loadingDialog
import com.barikoi.cnlapp.utils.extension.setHapticClickListener
import com.barikoi.cnlapp.utils.extension.toast
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject


@AndroidEntryPoint
class SummaryDetailsActivity : BaseActivity() {
    private lateinit var binding: ActivitySummaryDetails2Binding

    private val viewModel: SummaryDetailsViewModel by viewModels()

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    var users: List<UserSummary>? = emptyList()

    private lateinit var viewPagerAdapter: ViewPagerAdapter

    private lateinit var downloadLoader: Dialog

    private var formattedStartDate = ""
    private var formattedEndDate = ""

    val constraints = CalendarConstraints.Builder()
        .setValidator(DateValidatorPointBackward.now())
        .build()

    val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
        .setTitleText("Select Date Range")
        .setCalendarConstraints(constraints)
        .build()

    var soCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySummaryDetails2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog {
            downloadLoader = it
        }

        binding.toolbar.tvTitle.text = getString(R.string.summary_details)
        binding.toolbar.btnBack.setHapticClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val cal = Calendar.getInstance()

        formattedStartDate = cal.time.formatDateWithLocaleEnglish()
        formattedEndDate =  cal.time.formatDateWithLocaleEnglish()

        binding.tvDateRangeStart.text = formattedStartDate.formatDateWithDDmmYYYY()
        binding.tvDateRangeEnd.text = formattedEndDate.formatDateWithDDmmYYYY()

        users = intent.getParcelableArrayListCompat<UserSummary>("user_summary")
        soCount = intent.getIntExtra("position", 0)

        if (users.isNullOrEmpty()) {
            toast("No users found")
            return
        }

        binding.tvSOName.text = users?.get(soCount)?.name.toString()

        binding.ivPrev.isEnabled = soCount != 0

        binding.ivPrev.setHapticClickListener {
            if (soCount > 0) {
                soCount--
                binding.tvSOName.text = users?.get(soCount)?.name.toString()
                binding.ivPrev.isEnabled = soCount > 0


                sendDataToViewPagerFragments(
                    Pair(
                        formattedStartDate.getStartDateTime(),
                        formattedEndDate.getEndDateTime()
                    ), users!![soCount].id
                )
            }
        }

        binding.ivNext.setHapticClickListener {
            if (users != null && soCount < users!!.size - 1) {
                soCount++
                binding.tvSOName.text = users!![soCount].name
                binding.ivPrev.isEnabled = soCount > 0

                sendDataToViewPagerFragments(
                    Pair(
                        formattedStartDate.getStartDateTime(),
                        formattedEndDate.getEndDateTime()
                    ), users!![soCount].id
                )
            }
        }

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDateMillis = selection.first
            val endDateMillis = selection.second

            formattedStartDate = Date(startDateMillis!!).formatDateWithLocaleEnglish()
            formattedEndDate = Date(endDateMillis!!).formatDateWithLocaleEnglish()

            binding.tvDateRangeStart.text = formattedStartDate.formatDateWithDDmmYYYY()
            binding.tvDateRangeEnd.text = formattedEndDate.formatDateWithDDmmYYYY()

            sendDataToViewPagerFragments(
                Pair(
                    formattedStartDate.getStartDateTime(),
                    formattedEndDate.getEndDateTime()
                ), users!![soCount].id
            )

        }

        binding.tvDateRangeStart.setHapticClickListener {
            dateRangePicker.show(supportFragmentManager, "date_range_picker")
        }
        binding.tvDateRangeEnd.setHapticClickListener {
            dateRangePicker.show(supportFragmentManager, "date_range_picker")
        }

        viewPagerAdapter = ViewPagerAdapter(
            supportFragmentManager, lifecycle, listOf(
                SoDetailsSummaryFragment(),
                OutletTypeSummaryFragment()
            )
        )

        iniTabs()

        binding.btnDownload.setHapticClickListener {
            viewModel.downloadChalan(
                formattedStartDate.getStartDateTime(),
                formattedEndDate.getEndDateTime(),
                users!![soCount].id
            )
        }

        startOrdersObserve()
    }

    private fun iniTabs() {
        val previousTab = binding.viewPager.currentItem

        binding.viewPager.adapter = viewPagerAdapter

        binding.dotsIndicator.attachTo(binding.viewPager)

        binding.viewPager.post {
            binding.viewPager.setCurrentItem(previousTab, false)
        }

        sendDataToViewPagerFragments(
            Pair(
                formattedStartDate.getStartDateTime(),
                formattedEndDate.getEndDateTime()
            ), users!![soCount].id
        )
    }

    private fun sendDataToViewPagerFragments(data: Pair<String, String>, userId: String) {
        for (fragment in viewPagerAdapter.fragments) {
            if (fragment is DateFilterListener) {
                fragment.onDataReceived(data, userId)
            } else {
                AppLogger.log("Else")
            }
        }
    }

    private fun startOrdersObserve() {
        lifecycleScope.launch {
            viewModel.downloadResponse.observe(this@SummaryDetailsActivity) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startOrdersObserve::Empty")
                        downloadLoader.dismiss()
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startOrdersObserve::Error ${it.error}")
                        downloadLoader.dismiss()
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startOrdersObserve::Loading")
                        if (!downloadLoader.isShowing) {
                            downloadLoader.show()
                        }
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startOrdersObserve:: Success ${it.data}")
                        downloadLoader.dismiss()

                        NotificationUtils.showNotification(
                            this@SummaryDetailsActivity,
                            "Download Complete",
                            it.data!!.message!!,
                            it.data.fileName!!,
                            Constants.MIME_TYPE_PDF
                        )
                    }
                }
            }
        }
    }
}