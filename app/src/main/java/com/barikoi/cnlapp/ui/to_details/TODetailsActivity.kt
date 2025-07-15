package com.barikoi.cnlapp.ui.to_details

import android.os.Bundle
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.ac.BaseActivity
import com.barikoi.cnlapp.databinding.ActivityTodetailsBinding
import com.barikoi.cnlapp.ui.home.HomeFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TODetailsActivity : BaseActivity() {
    private lateinit var binding: ActivityTodetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTodetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, HomeFragment())
        fragmentTransaction.commit()
    }
}