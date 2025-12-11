package com.barikoi.cnlapp.ui.order_delivery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.databinding.FragmentPendingBinding
import com.barikoi.cnlapp.ui.order_delivery.vm.OrderDeliveryViewModel
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.extension.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PendingFragment : Fragment() {
    private lateinit var binding: FragmentPendingBinding
    private val viewModel: OrderDeliveryViewModel by activityViewModels()

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPendingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startSavedOrderObserve()
    }

    private fun startSavedOrderObserve() {
        lifecycleScope.launch {
            viewModel.orderResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startSavedOrderObserve::Empty")
                        binding.progressBar.isVisible = false
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startSavedOrderObserve::Error ${it.error}")
                        binding.progressBar.isVisible = false

                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startSavedOrderObserve::Loading")
                        binding.progressBar.isVisible = true
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startSavedOrderObserve:: Success ${it.data}")
                        binding.progressBar.isVisible = false
                    }
                }
            }
        }
    }
}