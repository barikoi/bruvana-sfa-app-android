package com.barikoi.cnlapp.request

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.data.remote.models.RequestStock
import com.barikoi.cnlapp.databinding.ItemStockRequestBinding
import com.barikoi.cnlapp.utils.Constants
import com.barikoi.cnlapp.utils.extension.convertDate
import com.barikoi.cnlapp.utils.extension.setHapticClickListener

class AdapterStockRequest(
    private val onItemClicked: (RequestStock) -> Unit
) : Adapter<AdapterStockRequest.AdapterRequestViewHolder>() {

    private var stockRequests: List<RequestStock> = emptyList()

    inner class AdapterRequestViewHolder(val binding: ItemStockRequestBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterRequestViewHolder {
        return AdapterRequestViewHolder(
            ItemStockRequestBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = stockRequests.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateStockRequests(stockRequests: List<RequestStock>) {
        this.stockRequests = stockRequests
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: AdapterRequestViewHolder, position: Int) {

        holder.binding.root.setHapticClickListener {
            onItemClicked(stockRequests[position])
        }

        holder.binding.tvTitle.text =
            stockRequests[position].dbHouse?.dbHouseName
        holder.binding.tvQuantity.text = stockRequests[position].stockProducts.size.toString()
        holder.binding.tvRequestTo.text = stockRequests[position].requestToUser.userName
        holder.binding.tvDate.text = stockRequests[position].createdAt.convertDate()

        when (stockRequests[position].status) {
            Constants.STATUS_DECLINED -> {
                holder.binding.tvStatus.text =
                    holder.binding.root.context.getString(R.string.declined)
                holder.binding.tvStatus.background = AppCompatResources.getDrawable(
                    holder.binding.root.context,
                    R.drawable.shape_with_corner_red
                )
                holder.binding.llStatus.background = AppCompatResources.getDrawable(
                    holder.binding.root.context,
                    R.drawable.stroke_with_corner_red
                )
            }

            Constants.STATUS_APPROVED -> {
                holder.binding.tvStatus.text =
                    holder.binding.root.context.getString(R.string.approved)
                holder.binding.tvStatus.background = AppCompatResources.getDrawable(
                    holder.binding.root.context,
                    R.drawable.shape_with_corner_green
                )
                holder.binding.llStatus.background = AppCompatResources.getDrawable(
                    holder.binding.root.context,
                    R.drawable.stroke_with_corner_green
                )
            }

            Constants.STATUS_PARTIAL_APPROVED -> {
                holder.binding.tvStatus.text =
                    holder.binding.root.context.getString(R.string.partial_approved)
                holder.binding.tvStatus.background = AppCompatResources.getDrawable(
                    holder.binding.root.context,
                    R.drawable.shape_with_corner_green
                )
                holder.binding.llStatus.background = AppCompatResources.getDrawable(
                    holder.binding.root.context,
                    R.drawable.stroke_with_corner_green
                )
            }

            else -> {
                holder.binding.tvStatus.text =
                    holder.binding.root.context.getString(R.string.pending)
                holder.binding.tvStatus.background = AppCompatResources.getDrawable(
                    holder.binding.root.context,
                    R.drawable.shape_with_corner_yellow
                )
                holder.binding.llStatus.background = AppCompatResources.getDrawable(
                    holder.binding.root.context,
                    R.drawable.stroke_with_corner_yellow
                )
            }
        }
    }
}

