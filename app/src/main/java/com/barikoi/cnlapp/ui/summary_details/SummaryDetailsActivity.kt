package com.barikoi.cnlapp.ui.summary_details

import android.os.Bundle
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.ac.BaseActivity
import com.barikoi.cnlapp.data.remote.models.ActiveInactiveUser
import com.barikoi.cnlapp.data.remote.models.UserSummary
import com.barikoi.cnlapp.databinding.ActivitySummaryDetailsBinding
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.Constants
import com.barikoi.cnlapp.utils.extension.formatDateWithDDMM
import com.barikoi.cnlapp.utils.extension.formatDateWithLocale
import com.barikoi.cnlapp.utils.extension.getParcelableArrayListCompat
import com.barikoi.cnlapp.utils.extension.setHapticClickListener
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Date

class SummaryDetailsActivity : BaseActivity() {
    private lateinit var binding: ActivitySummaryDetailsBinding


    private var formattedStartDate = ""
    private var formattedEndDate = ""

    val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
        .setTitleText("Select Date Range")
        .build()

    var soCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySummaryDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.tvTitle.text = getString(R.string.summary_details)
        binding.toolbar.btnBack.setHapticClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        formattedStartDate = Date().formatDateWithLocale()
        formattedEndDate = Date().formatDateWithLocale()

        binding.tvDateRangeStart.text = formattedStartDate.formatDateWithDDMM()
        binding.tvDateRangeEnd.text = formattedEndDate.formatDateWithDDMM()

        val users = intent.getParcelableArrayListCompat<UserSummary>("user_summary")

        binding.tvSOName.text = users?.get(soCount)?.name.toString()
        binding.ivPrev.isEnabled = soCount > 0
        binding.ivPrev.isEnabled = soCount == users!!.size

        binding.ivPrev.setHapticClickListener {
            if (soCount > 0) {
                soCount--
                binding.tvSOName.text = users?.get(soCount)?.name.toString()
                binding.ivPrev.isEnabled = soCount > 0
            }
        }

        binding.ivNext.setHapticClickListener {
            if (users != null && soCount < users.size - 1) {
                soCount++
                binding.tvSOName.text = users[soCount].name
                binding.ivPrev.isEnabled = soCount == users.size
            }
        }

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDateMillis = selection.first
            val endDateMillis = selection.second

            formattedStartDate = Date(startDateMillis!!).formatDateWithLocale()
            formattedEndDate = Date(endDateMillis!!).formatDateWithLocale()


        }

        binding.tvDateRangeStart.setHapticClickListener {
            dateRangePicker.show(supportFragmentManager, "date_range_picker")
        }
        binding.tvDateRangeEnd.setHapticClickListener {
            dateRangePicker.show(supportFragmentManager, "date_range_picker")
        }
    }
}