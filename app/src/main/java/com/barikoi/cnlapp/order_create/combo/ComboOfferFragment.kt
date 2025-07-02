package com.barikoi.cnlapp.order_create.combo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.data.remote.models.offer.Offer
import com.barikoi.cnlapp.databinding.FragmentComboOfferBinding
import com.barikoi.cnlapp.order_create.product_selection.vm.ProductSelectViewModel
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.extension.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ComboOfferFragment(
    private var viewModel: ProductSelectViewModel
) : Fragment() {
    private lateinit var binding: FragmentComboOfferBinding

    private lateinit var adapterOffer: AdapterOffer

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    private var offers: MutableList<Offer> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentComboOfferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getOffers()
        startOfferObserve()

        adapterOffer = AdapterOffer(
            onPlusClick = { offer, position ->
                offer.quantity += 1
                adapterOffer.updateQuantity(offer, position)
                viewModel.setOffers(offers)

            },
            onMinusClick = { offer, position ->
                if (offer.quantity > 0) {
                    offer.quantity -= 1
                    adapterOffer.updateQuantity(offer, position)
                    viewModel.setOffers(offers)
                }
            }
        )
        val layoutManager = LinearLayoutManager(context)
        binding.rcvOffer.layoutManager = layoutManager
        binding.rcvOffer.adapter = adapterOffer
        binding.rcvOffer.itemAnimator = null

//        val itemAnimator =  binding.rcvOffer.itemAnimator
//        if (itemAnimator is SimpleItemAnimator) {
//            itemAnimator.supportsChangeAnimations = false
//        }
    }

    private fun startOfferObserve() {
        lifecycleScope.launch {
            viewModel.offerResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("startOfferObserve::Empty")
                    }

                    is ApiState.Error -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("startOfferObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        binding.progressBar.isVisible = true
                        AppLogger.log("startOfferObserve::Loading")
                    }

                    is ApiState.Success -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("startOfferObserve:: Success ${it.data}")

                        offers = it.data!!.offers.toMutableList()
                        adapterOffer.updateData(it.data.offers)


                    }
                }
            }
        }
    }
}