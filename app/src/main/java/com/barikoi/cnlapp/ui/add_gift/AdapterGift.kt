package com.barikoi.cnlapp.ui.add_gift

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.barikoi.cnlapp.base.adapter.AdapterImagePickerView
import com.barikoi.cnlapp.databinding.ItemAddGiftBinding
import com.barikoi.cnlapp.utils.extension.setHapticClickListener

class AdapterGift(
    private val addImageClickListener: (Int) -> Unit,
    private val removeImageClickListener: (Int) -> Unit,
    private val removeItemClickListener: (Int) -> Unit
) : Adapter<AdapterGift.GiftViewHolder>() {

    private var giftList: List<GiftDataModel> = emptyList()

    private var adapterImage: AdapterImagePickerView = AdapterImagePickerView {

    }

    inner class GiftViewHolder(val binding: ItemAddGiftBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GiftViewHolder {
        return GiftViewHolder(
            ItemAddGiftBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = giftList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(list: List<GiftDataModel>) {
        giftList = list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: GiftViewHolder, position: Int) {
        holder.binding.tvGiftName.text = giftList[position].name

        holder.binding.llImagePickerView.rvImage.adapter = adapterImage

        val layoutManager = LinearLayoutManager(
            holder.binding.llImagePickerView.rvImage.context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        holder.binding.llImagePickerView.rvImage.layoutManager = layoutManager

        adapterImage.updateImages(giftList[position].images)

        holder.binding.llImagePickerView.ivPicImage.setOnClickListener {
            addImageClickListener(position)
        }

        holder.binding.ivClose.setHapticClickListener {
            removeItemClickListener(position)
        }
    }
}