package com.barikoi.cnlapp.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.databinding.FragmentHome2Binding
import com.barikoi.cnlapp.ui.home.vm.HomeViewModel
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.extension.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHome2Binding

    private val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHome2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        startActiveInactiveUserObserve()

        viewModel.getActiveInactiveUsers(
            "2025-07-07", "2025-07-07"
        )


    }

    private fun startActiveInactiveUserObserve() {
        lifecycleScope.launch {
            viewModel.activeInactiveResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startActiveInactiveUserObserve::Empty")
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startActiveInactiveUserObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))

                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startActiveInactiveUserObserve::Loading")
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startActiveInactiveUserObserve:: Success ${it.data}")

                        binding.tvActiveValue.text = it.data!!.active.size.toString()
                        binding.tvInactiveValue.text = it.data.inactive.size.toString()
                    }
                }
            }
        }
    }
}