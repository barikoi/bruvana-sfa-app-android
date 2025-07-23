package com.barikoi.cnlapp.ui.summary_details.fragment.so

import com.barikoi.cnlapp.R

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.StatisticsHome.Model.SoStats
import com.barikoi.cnlapp.databinding.ItemSoStatsBinding

class AdapterSoStats : RecyclerView.Adapter<AdapterSoStats.SoStatsViewHolder>() {
    private var summary: List<SoStats> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SoStatsViewHolder {
        return SoStatsViewHolder(
            ItemSoStatsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
    //৳

    override fun onBindViewHolder(
        holder: SoStatsViewHolder,
        position: Int
    ) {
        holder.binding.tvName.text = summary[position].title
        if (position == 0 || position == 1) {
            holder.binding.tvValue.text = holder.binding.tvValue.context.getString(
                R.string.taka_sign,
                summary[position].value
            )
        } else {
            holder.binding.tvValue.text = summary[position].value
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newSummary: List<SoStats>) {
        summary = newSummary
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = summary.size

    inner class SoStatsViewHolder(val binding: ItemSoStatsBinding) :
        RecyclerView.ViewHolder(binding.root)
}