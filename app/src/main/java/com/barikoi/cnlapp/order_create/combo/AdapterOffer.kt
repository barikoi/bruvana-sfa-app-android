package com.barikoi.cnlapp.order_create.combo

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.barikoi.cnlapp.data.remote.models.offer.Offer
import com.barikoi.cnlapp.databinding.ItemOfferViewBinding
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.extension.setHapticClickListener

class AdapterOffer(
    private val onPlusClick: (Offer, Int) -> Unit,
    private val onMinusClick: (Offer, Int) -> Unit
) : ListAdapter<Offer, AdapterOffer.OfferViewHolder>(OfferDiffCallback()) {

    init {
        setHasStableIds(true)
    }

    private var offers: MutableList<Offer> = mutableListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OfferViewHolder {
        return OfferViewHolder(
            ItemOfferViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            holder.binding.tvCount.setText(
                offers[position].quantity.toString()
            )
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onBindViewHolder(
        holder: OfferViewHolder,
        position: Int
    ) {
        holder.binding.tvProductName.text = offers[position].name
        holder.binding.tvPrice.text = offers[position].originalPrice
        holder.binding.tvSubTotal.text = offers[position].comboPrice
        holder.binding.tvDescription.text = offers[position].productCombinations
            .mapIndexed { index, it -> "${index + 1}. ${it.product.name.trim()} X ${it.quantity}" }
            .joinToString("\n")

        holder.binding.tvCount.setText(
            offers[position].quantity.toString()
        )

        holder.binding.btnPlus.setHapticClickListener {
            onPlusClick(offers[position], position)
        }

        holder.binding.btnMinus.setHapticClickListener {
            onMinusClick(offers[position], position)
        }
    }

    override fun getItemCount(): Int = offers.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newOffers: List<Offer>) {
        AppLogger.log("AdapterOffer::updateData newOffers: $newOffers")
        offers = newOffers.toMutableList()
        notifyDataSetChanged()
    }

    fun updateQuantity(offer: Offer, position: Int) {
        AppLogger.log("AdapterOffer::updateQuantity position: $position, offer: $offer")
        offers[position] = offer
        notifyItemChanged(position, "quantity_updated")
    }


    override fun getItemId(position: Int): Long {
        return offers[position].id.toLong()
    }

    inner class OfferViewHolder(val binding: ItemOfferViewBinding) : ViewHolder(binding.root)
}