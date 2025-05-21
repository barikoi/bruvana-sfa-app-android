package com.barikoi.cnlapp.StatisticsHome.Adapter

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.StatisticsHome.Model.TargetValue
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlin.math.roundToInt

class TargetAdapter(private val targets: List<TargetValue>, val from: String) :
    RecyclerView.Adapter<TargetAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.single_summary_view, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        holder.tvTitle.text = targets[position].title
        holder.targetedAmount.text = targets[position].target
        holder.completedAmount.text = targets[position].completed

        if (from.equals("TO", true)) {
            holder.itemView.background.setTint(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.target_bg_color
                )
            )
        } else if (from.equals("SO", true)) {
            holder.itemView.background.setTint(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.card_blue
                )
            )
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (targets[position].target.equals("--:--")) {
                    targets[position].target = "0"
                }
                if (targets[position].completed.equals("--:--")) {
                    targets[position].completed = "0"
                }
                holder.progressView.max = targets[position].targetValue.roundToInt().toInt()
                holder.progressView.setProgress(
                    targets[position].completedValue.roundToInt().toInt(), true
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("Target", "exception: " + e.message)
        }

    }

    override fun getItemCount(): Int {
        return targets.size
    }

    public class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        internal val targetedAmount: TextView = itemView.findViewById(R.id.targetedAmount)
        internal val completedAmount: TextView = itemView.findViewById(R.id.completeAmount)
        internal val progressView: LinearProgressIndicator = itemView.findViewById(R.id.progressBar)
    }
}