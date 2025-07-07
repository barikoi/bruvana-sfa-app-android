package com.barikoi.cnlapp.ui.active_inactive

import android.os.Bundle
import com.barikoi.cnlapp.base.ac.BaseActivity
import com.barikoi.cnlapp.databinding.ActivityActiveInactive2Binding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActiveInactiveActivity : BaseActivity() {
    private lateinit var binding: ActivityActiveInactive2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityActiveInactive2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.toolbar.tvTitle.text = intent.getStringExtra("so_status")
    }
}