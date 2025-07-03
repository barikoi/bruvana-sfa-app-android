package com.barikoi.cnlapp.ui.approval.adapter

import android.annotation.SuppressLint
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.data.remote.models.RequestStock
import com.barikoi.cnlapp.databinding.ItemStockApprovalBinding
import com.barikoi.cnlapp.utils.Constants
import com.barikoi.cnlapp.utils.extension.formatHumanReadableDate
import com.barikoi.cnlapp.utils.extension.formatHumanReadableTime
import com.barikoi.cnlapp.utils.extension.setHapticClickListener

class AdapterApproval(
    private val type: String,
    private val onItemClickListener: (RequestStock) -> Unit
) : RecyclerView.Adapter<AdapterApproval.ShopRequestViewHolder>() {

    private var shopRequests: List<RequestStock> = emptyList()

    inner class ShopRequestViewHolder(val binding: ItemStockApprovalBinding) :
        ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopRequestViewHolder {
        return ShopRequestViewHolder(
            ItemStockApprovalBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = shopRequests.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateShopRequests(shopRequests: List<RequestStock>) {
        this.shopRequests = shopRequests
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ShopRequestViewHolder, position: Int) {
        val text = buildSpannedString {
            bold { append(shopRequests[position].requestFromUser.userName) }.setSpan(
                ForegroundColorSpan(holder.binding.root.context.resources.getColor(R.color.cnl_color_1)),
                0, this.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            append(" is requesting you for")
            if (type == Constants.OUTLET) bold { append(" Create New Shop Request ") } else
                bold { append(" Product Stock Request ") }
            append(" approval")
        }

        holder.binding.tvSoName.text = text
        holder.binding.tvTimeOnly.text = shopRequests[position].createdAt.formatHumanReadableTime()
        holder.binding.tvTime.text = shopRequests[position].createdAt.formatHumanReadableDate()

        holder.binding.root.setHapticClickListener {
            onItemClickListener(shopRequests[position])
        }

        when (shopRequests[position].status) {
            Constants.STATUS_PENDING -> {
                holder.binding.viewLine.isVisible = true

                holder.binding.llMain.background = AppCompatResources.getDrawable(
                    holder.binding.root.context,
                    R.color.green_color_light
                )
            }

            else -> {
                holder.binding.viewLine.isVisible = false

                holder.binding.llMain.background = AppCompatResources.getDrawable(
                    holder.binding.root.context,
                    R.color.white
                )
            }
        }
    }
}