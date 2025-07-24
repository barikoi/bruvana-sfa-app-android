package com.barikoi.cnlapp.ui.active_inactive

import android.os.Bundle
import com.barikoi.cnlapp.base.ac.BaseActivity
import com.barikoi.cnlapp.data.remote.models.ActiveInactiveUser
import com.barikoi.cnlapp.databinding.ActivityActiveInactive2Binding
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.extension.getParcelableArrayListCompat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActiveInactiveActivity : BaseActivity() {
    private lateinit var binding: ActivityActiveInactive2Binding

    private lateinit var adapterActiveInactive: AdapterActiveInactive

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityActiveInactive2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        adapterActiveInactive = AdapterActiveInactive(intent.getStringExtra("so_status")!!)

        binding.toolbar.tvTitle.text = intent.getStringExtra("so_status")

        val users = intent.getParcelableArrayListCompat<ActiveInactiveUser>("users")

        binding.rcvActiveInactiveUsers.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.rcvActiveInactiveUsers.adapter = adapterActiveInactive
        if (users != null) {
            adapterActiveInactive.updateList(users)
        } else {
            AppLogger.log("ActiveInactiveActivity::onCreate users is null")
        }

    }
}