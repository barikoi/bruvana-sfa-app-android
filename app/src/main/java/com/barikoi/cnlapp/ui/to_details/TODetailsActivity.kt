package com.barikoi.cnlapp.ui.to_details

import android.os.Bundle
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.ac.BaseActivity
import com.barikoi.cnlapp.data.remote.models.UserSummary
import com.barikoi.cnlapp.databinding.ActivityTodetailsBinding
import com.barikoi.cnlapp.ui.home.HomeFragment
import com.barikoi.cnlapp.utils.extension.parcelable
import com.barikoi.cnlapp.utils.extension.setHapticClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TODetailsActivity : BaseActivity() {
    private lateinit var binding: ActivityTodetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTodetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = intent.parcelable<UserSummary>("user_summary")

        binding.toolbar.tvTitle.text = user?.name.toString()
        binding.toolbar.btnBack.setHapticClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, HomeFragment("TO", user!!.territoryId.toString(), user.id))
        fragmentTransaction.commit()
    }
}