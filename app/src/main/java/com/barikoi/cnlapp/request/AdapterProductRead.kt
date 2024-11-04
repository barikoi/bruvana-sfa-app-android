package com.barikoi.cnlapp.request

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.data.remote.models.StockProduct
import com.barikoi.cnlapp.databinding.ItemProductReadBinding
import com.barikoi.cnlapp.utils.AppLogger

class AdapterProductRead : Adapter<AdapterProductRead.ApproveViewHolder>() {

    private var requests: List<StockProduct> = emptyList()

    inner class ApproveViewHolder(val binding: ItemProductReadBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApproveViewHolder {
        return ApproveViewHolder(
            ItemProductReadBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = requests.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateApproveData(requests: List<StockProduct>) {
        this.requests = requests
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ApproveViewHolder, position: Int) {
        holder.binding.tvProductName.text = "${position + 1}. ${requests[position].productName}"
        holder.binding.etStockCount.text = requests[position].currentAvailableStock.toString()
        AppLogger.log("STATUS:: ${requests[position].status}")
        when (requests[position].status) {
            0 -> {
                holder.binding.etStockCount.setTextColor(
                    holder.itemView.context.resources.getColor(
                        R.color.text_title
                    )
                )
                holder.binding.tvProductStatus.text =
                    holder.itemView.context.getString(R.string.pending)
                holder.binding.tvProductStatus.background =
                    holder.itemView.context.resources.getDrawable(R.drawable.shape_with_corner_yellow_20)
                holder.binding.etStockCountApprove.isVisible = false
            }

            1 -> {
                holder.binding.etStockCount.setTextColor(
                    holder.itemView.context.resources.getColor(
                        R.color.text_title
                    )
                )
                holder.binding.tvProductStatus.text =
                    holder.itemView.context.getString(R.string.approved)
                holder.binding.tvProductStatus.background =
                    holder.itemView.context.resources.getDrawable(R.drawable.shape_with_corner_green_20)
                holder.binding.etStockCountApprove.isVisible = false
            }

            2 -> {
                holder.binding.etStockCount.setTextColor(
                    holder.itemView.context.resources.getColor(
                        R.color.text_title
                    )
                )
                holder.binding.tvProductStatus.text =
                    holder.itemView.context.getString(R.string.declined)
                holder.binding.tvProductStatus.background =
                    holder.itemView.context.resources.getDrawable(R.drawable.shape_with_corner_red_20)
                holder.binding.etStockCountApprove.isVisible = false

            }

            3 -> {
                holder.binding.etStockCount.setTextColor(
                    holder.itemView.context.resources.getColor(
                        R.color.text_title
                    )
                )
                holder.binding.tvProductStatus.text =
                    holder.itemView.context.getString(R.string.approved)
                holder.binding.tvProductStatus.background =
                    holder.itemView.context.resources.getDrawable(R.drawable.shape_with_corner_green_20)

                holder.binding.etStockCount.text =
                    requests[position].currentAvailableStock.toString()

                holder.binding.etStockCount.setTextColor(
                    holder.itemView.context.resources.getColor(
                        R.color.red200
                    )
                )
                holder.binding.etStockCount.setTextColor(
                    holder.itemView.context.resources.getColor(
                        R.color.text_title
                    )
                )
                holder.binding.etStockCount.paintFlags =
                    holder.binding.etStockCount.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                holder.binding.etStockCountApprove.isVisible = true
                holder.binding.etStockCountApprove.text = requests[position].approveStock.toString()
            }
        }

    }
}