package com.barikoi.cnlapp.ui.summary_details.fragment.outlet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.databinding.FragmentOutletTypeSummaryBinding
import com.barikoi.cnlapp.ui.summary_details.DateFilterListener
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.extension.setHapticClickListener
import com.barikoi.cnlapp.utils.extension.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OutletTypeSummaryFragment : Fragment(), DateFilterListener {
    private lateinit var binding: FragmentOutletTypeSummaryBinding
    private val viewModel: OutletTypeSummaryViewModel by viewModels()

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    var datePair: Pair<String, String> = Pair("", "")
    var userId: String = ""


    private lateinit var adapterOutletType: AdapterOutletType

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOutletTypeSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        adapterOutletType = AdapterOutletType()


        adapterOutletType = AdapterOutletType()
        binding.rcvOutletType.layoutManager = LinearLayoutManager(requireContext())
        binding.rcvOutletType.adapter = adapterOutletType

        startOutletTypeSummaryObserve()

        binding.btnRetry.setHapticClickListener {
            viewModel.getOutletTypeSummary(
                datePair.first,
                datePair.second,
                userId
            )
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        viewModel.getOutletTypeSummary(
            datePair.first,
            datePair.second,
            userId
        )
    }

    private fun startOutletTypeSummaryObserve() {
        lifecycleScope.launch {
            viewModel.outletTypeSummaryResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startOutletTypeSummaryObserve::Empty")
                        binding.loadingSoStats.isVisible = false
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startOutletTypeSummaryObserve::Error ${it.error}")
                        binding.loadingSoStats.isVisible = false
                        binding.rcvOutletType.isVisible = false
                        if (it.data?.msg?.isNotEmpty() == true) {
                            toast(it.data.msg)
                            binding.llError.isVisible = true
                            binding.tvNoDataFound.text = it.data.msg
                            return@observe
                        }
                        binding.llError.isVisible = true
                        binding.tvNoDataFound.text = networkFailureMessage.handleFailure(it.error!!)
                        toast(networkFailureMessage.handleFailure(it.error))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startOutletTypeSummaryObserve::Loading")
                        binding.loadingSoStats.isVisible = true
                        binding.llError.isVisible = false
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startOutletTypeSummaryObserve:: Success ${it.data}")
                        binding.loadingSoStats.isVisible = false
                        binding.rcvOutletType.isVisible = true
                        binding.llError.isVisible = false


                        adapterOutletType.updateData(it.data!!.data)
                    }
                }
            }
        }
    }

    override fun onDataReceived(
        data: Pair<String, String>,
        useId: String
    ) {
        datePair = data
        userId = useId
        if (isAdded) {
            viewModel.getOutletTypeSummary(
                datePair.first,
                datePair.second,
                useId
            )
        }
    }
}