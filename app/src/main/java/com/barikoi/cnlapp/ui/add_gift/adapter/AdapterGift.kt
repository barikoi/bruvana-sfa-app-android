package com.barikoi.cnlapp.ui.add_gift.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil3.load
import coil3.request.fallback
import coil3.request.placeholder
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.adapter.AdapterImagePickerView
import com.barikoi.cnlapp.data.remote.models.Gift
import com.barikoi.cnlapp.databinding.ItemAddGift2Binding
import com.barikoi.cnlapp.utils.extension.setHapticClickListener

class AdapterGift(
    private val addClickListener: (Int) -> Unit,
    private val incrementClickListener: (Int) -> Unit,
    private val decrementClickListener: (Int) -> Unit,
    private val removeItemClickListener: (Int) -> Unit
) : Adapter<AdapterGift.GiftViewHolder>() {

    private var giftList: List<Gift> = emptyList()

    private var adapterImage: AdapterImagePickerView = AdapterImagePickerView {

    }

    inner class GiftViewHolder(val binding: ItemAddGift2Binding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GiftViewHolder {
        return GiftViewHolder(
            ItemAddGift2Binding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int = giftList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(list: List<Gift>) {
        giftList = list
        notifyDataSetChanged()
    }

    fun updateByPos(pos: Int, list: List<Gift>) {
        giftList = list
        notifyItemChanged(pos)
    }

    override fun onBindViewHolder(holder: GiftViewHolder, position: Int) {
        holder.binding.tvGiftName.text = giftList[position].name

        holder.binding.tvCount.setText(giftList[position].qty.toString())

        if (giftList[position].images.isNullOrEmpty()) {
            holder.binding.tvImageCount.isVisible = false
        } else {
            holder.binding.tvImageCount.isVisible = true
            holder.binding.tvImageCount.text =
                holder.binding.tvImageCount.context.getString(
                    R.string.images_added,
                    giftList[position].images?.size
                )
        }

        holder.binding.ivGift.load(
            giftList[position].image,
        ) {
            placeholder(R.drawable.image_place_holder) // Correct way to set placeholder
            fallback(R.drawable.image_place_holder) // Optional: Show if URL is null
        }

        if (giftList[position].images.isNullOrEmpty()) {
            holder.binding.llCardCounter.isVisible = false
            holder.binding.btnAdd.isVisible = true
            holder.binding.ivDelete.isVisible = false
        } else {
            holder.binding.llCardCounter.isVisible = true
            holder.binding.btnAdd.isVisible = false
            holder.binding.ivDelete.isVisible = true
        }


        holder.binding.btnAdd.setHapticClickListener {
            addClickListener.invoke(position)
        }

        holder.binding.ivDelete.setHapticClickListener {
            removeItemClickListener.invoke(position)
        }

        holder.binding.btnPlus.setHapticClickListener {
            incrementClickListener.invoke(position)
        }

        holder.binding.btnMinus.setHapticClickListener {
            decrementClickListener.invoke(position)
        }
    }
}