package com.barikoi.cnlapp.order_create.combo

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.barikoi.cnlapp.data.remote.models.offer.Offer
import com.barikoi.cnlapp.databinding.ItemOfferViewBinding
import com.barikoi.cnlapp.utils.extension.setHapticClickListener

class AdapterOffer(
    private val onPlusClick: (Offer, Int) -> Unit,
    private val onMinusClick: (Offer, Int) -> Unit
) : RecyclerView.Adapter<AdapterOffer.OfferViewHolder>() {
    private var offers: List<Offer> = emptyList()

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
        offers = newOffers
        notifyDataSetChanged()
    }

    fun updateQuantity(offer: Offer, position: Int) {
        val updated = offers.toMutableList()
        updated[position] = offer
        offers = updated
        notifyItemChanged(position)
    }

    inner class OfferViewHolder(val binding: ItemOfferViewBinding) : ViewHolder(binding.root)
}