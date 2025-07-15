package com.barikoi.cnlapp.ui.home.adapter

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.StatisticsHome.Model.TargetAndCompleted
import com.barikoi.cnlapp.databinding.ItemSummaryBinding
import kotlin.math.roundToInt

class TargetAdapter(

) :
    RecyclerView.Adapter<TargetAdapter.TargetAdapterViewHolder>() {
    private var targets: List<TargetAndCompleted> = emptyList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TargetAdapterViewHolder {
        return TargetAdapterViewHolder(
            ItemSummaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: TargetAdapterViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        holder.binding.tvTitle.text = targets[position].title
        holder.binding.tvTargetedAmount.text = targets[position].target
        holder.binding.tvCompleteAmount.text = targets[position].completed

        holder.binding.progressBar.max = targets[position].target.toDouble().roundToInt()
        holder.binding.progressBar.setProgress(
            targets[position].completed.toDouble().roundToInt(), true
        )

//        if (from.equals("TO", true)) {
//            holder.itemView.background.setTint(
//                ContextCompat.getColor(
//                    holder.itemView.context,
//                    R.color.target_bg_color
//                )
//            )
//        } else if (from.equals("SO", true)) {
//            holder.itemView.background.setTint(
//                ContextCompat.getColor(
//                    holder.itemView.context,
//                    R.color.card_blue
//                )
//            )
//        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newTargets: List<TargetAndCompleted>) {
        targets = newTargets
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = targets.size


    inner class TargetAdapterViewHolder(val binding: ItemSummaryBinding) :
        RecyclerView.ViewHolder(binding.root)
}