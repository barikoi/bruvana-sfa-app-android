package com.barikoi.cnlapp.ui.summary_details.fragment.so

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.barikoi.cnlapp.StatisticsHome.Model.SoStats
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.databinding.FragmentSoDetailsSummaryBinding
import com.barikoi.cnlapp.ui.summary_details.DateFilterListener
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.extension.setHapticClickListener
import com.barikoi.cnlapp.utils.extension.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class SoDetailsSummaryFragment : Fragment(), DateFilterListener {
    private lateinit var binding: FragmentSoDetailsSummaryBinding
    private val viewModel: SoSummaryViewModel by viewModels()

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage


    private lateinit var adapterSoStats: AdapterSoStats

    var datePair: Pair<String, String> = Pair("", "")
    var userId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSoDetailsSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        adapterSoStats = AdapterSoStats()
        binding.rcvSoStats.layoutManager = LinearLayoutManager(requireContext())
        binding.rcvSoStats.adapter = adapterSoStats

        val dividerItemDecoration = DividerItemDecoration(
            binding.rcvSoStats.context,
            LinearLayoutManager.VERTICAL // অথবা HORIZONTAL
        )
        binding.rcvSoStats.addItemDecoration(dividerItemDecoration)

        startSoSummaryObserve()

        binding.btnRetry.setHapticClickListener {
            viewModel.getSOSummary(
                datePair.first,
                datePair.second,
                userId
            )
        }
    }

    private fun startSoSummaryObserve() {
        lifecycleScope.launch {
            viewModel.soStatsResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startSoSummaryObserve::Empty")
                        binding.loadingSoStats.isVisible = false
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startSoSummaryObserve::Error ${it.error}")
                        binding.loadingSoStats.isVisible = false
                        if (it.data?.message?.isNotEmpty() == true) {
                            toast(it.data.message)
                            binding.llError.isVisible = true
                            binding.tvNoDataFound.text = it.data.message
                            return@observe
                        }
                        toast(networkFailureMessage.handleFailure(it.error!!))

                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startSoSummaryObserve::Loading")
                        binding.loadingSoStats.isVisible = true
                        binding.llError.isVisible = false
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startSoSummaryObserve:: Success ${it.data}")
                        binding.loadingSoStats.isVisible = false
                        binding.llError.isVisible = false

                        val soStats = listOf(
                            SoStats(
                                "Order value",
                                String.format(Locale.getDefault(), "%,.2f", "${it.data!!.targetCompleted[0].revenue ?: 0}")
                            ),
                            SoStats(
                                "Delivery value",
                                String.format(Locale.getDefault(), "%,.2f", "${it.data!!.targetCompleted[0].deliveredValue ?: 0}")
                            ),
                            SoStats(
                                "Number of Visits",
                                it.data.targetCompleted[0].numberOfVisits.toString()
                            ),
                            SoStats(
                                "Number of Memo",
                                it.data.targetCompleted[0].numberOfMemo.toString()
                            ),
                            SoStats(
                                "SKU Per Memo",
                                it.data.targetCompleted[0].skuPerMemo ?: "0"
                            ),
                            SoStats(
                                "ADS",
                                String.format(Locale.getDefault(), "%,.2f", "${it.data.targetCompleted[0].ads ?: 0}")
                            ),
                            SoStats(
                                "RDS",
                                String.format(Locale.getDefault(), "%,.2f", "${it.data!!.targetCompleted[0].rds ?: 0}")
                            ),
                            SoStats(
                                "Bounce %",
                                it.data.targetCompleted[0].bounceAmountPercentage.toString()
                            )
                        )

                        adapterSoStats.updateData(soStats)
                    }
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        viewModel.getSOSummary(
            datePair.first,
            datePair.second,
            userId
        )
    }

    override fun onDataReceived(
        data: Pair<String, String>,
        useId: String
    ) {
        AppLogger.log("${this::class.simpleName}:: onDataReceived:: $data")
        datePair = data
        userId = useId

        if (isAdded) {
            viewModel.getSOSummary(
                datePair.first,
                datePair.second,
                userId
            )
        }
    }
}