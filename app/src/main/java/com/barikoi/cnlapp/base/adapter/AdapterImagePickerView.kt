package com.barikoi.cnlapp.base.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import coil3.load
import com.barikoi.cnlapp.databinding.ItemImageBinding

class AdapterImagePickerView(
    private val onImageCloseClick: (Int) -> Unit
) : Adapter<AdapterImagePickerView.ImagePickerViewHolder>() {

    var images: List<String> = emptyList()

    inner class ImagePickerViewHolder(val binding: ItemImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagePickerViewHolder {
        return ImagePickerViewHolder(
            ItemImageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int = images.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateImages(images: List<String>) {
        this.images = images
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ImagePickerViewHolder, position: Int) {
        holder.binding.ivPicImage.load(
            images[position]
        )

        holder.binding.ivClose.setOnClickListener {
            onImageCloseClick(position)
        }
    }
}