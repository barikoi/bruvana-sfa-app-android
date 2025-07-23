package com.barikoi.cnlapp.ui.summary_details.fragment.outlet

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.data.remote.models.OutletTypeData
import com.barikoi.cnlapp.databinding.ItemShopTypeWiseStatsBinding
import com.barikoi.cnlapp.utils.extension.setHapticClickListener
import java.util.Locale

class AdapterOutletType : RecyclerView.Adapter<AdapterOutletType.OutletTypeViewHolder>() {
    private var outletTypeList: List<OutletTypeData> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): OutletTypeViewHolder {
        return OutletTypeViewHolder(
            ItemShopTypeWiseStatsBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(
        holder: OutletTypeViewHolder, position: Int
    ) {
        val outletType = outletTypeList[position]
        with(holder.binding) {
            tvName.text = outletType.outletType
            tvQty.text = "QTY: " + outletType.totalOutlet.toString()

            tvVisitValue.text = outletType.totalVisits.toString()
            tvOrderValue.text = String.format(Locale.getDefault(), "%,.2f", outletType.orderAmount.toDoubleOrNull() ?: 0.0)
            tvAivValue.text = String.format(Locale.getDefault(), "%,.2f", outletType.aiv.toDoubleOrNull() ?: 0.0)

            tvQTYPresenceValue.text = outletType.qtyPresence
            tvDeliveryValue.text =String.format(Locale.getDefault(), "%,.2f", outletType.deliveryAmount?.toDoubleOrNull() ?: 0.0)
            tvContributionValue.text = outletType.contribution.toString()
        }

        if (outletType.isExpanded) {
            holder.binding.llDetails.isVisible = true
            holder.binding.ivExpend.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.binding.ivExpend.context,
                    R.drawable.ic__arrow_up
                )
            )

        } else {
            holder.binding.llDetails.isVisible = false
            holder.binding.ivExpend.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.binding.ivExpend.context,
                    R.drawable.ic_arrow_down
                )
            )
        }

        holder.binding.ivExpend.setHapticClickListener {
            outletType.isExpanded = !outletType.isExpanded
            notifyItemChanged(position)
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<OutletTypeData>) {
        outletTypeList = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = outletTypeList.size

    inner class OutletTypeViewHolder(val binding: ItemShopTypeWiseStatsBinding) :
        RecyclerView.ViewHolder(binding.root)
}