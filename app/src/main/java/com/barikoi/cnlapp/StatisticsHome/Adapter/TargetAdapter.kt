package com.barikoi.cnlapp.StatisticsHome.Adapter

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.StatisticsHome.Model.TargetValue
import com.google.android.material.progressindicator.LinearProgressIndicator

class TargetAdapter (val targets: List<TargetValue>, val from: String) : RecyclerView.Adapter<TargetAdapter.ViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.single_summary_view, parent, false)
        return ViewHolder(v)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.tvTitle.setText(targets[position].title)
        holder.targetedAmount.setText(targets[position].target)
        holder.completedAmount.setText(targets[position].completed)

        if (from.equals("TO", true)){
            holder.itemView.background.setTint(holder.itemView.resources.getColor(R.color.cnl_color_1))
        }else if (from.equals("SO", true)){
            holder.itemView.background.setTint(holder.itemView.resources.getColor(R.color.card_blue))
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (targets[position].target.equals("--:--")){
                    targets[position].target = "0"
                }
                if (targets[position].completed.equals("--:--")){
                    targets[position].completed = "0"
                }
                holder.progressView.max = Math.round(targets[position].target.toDouble()).toInt()
                holder.progressView.setProgress(Math.round(targets[position].completed.toDouble()).toInt(), true)

                Log.d("Target", "target: " + Math.round(targets[position].target.toDouble()).toInt())
                Log.d("Target", "completed: " + Math.round(targets[position].completed.toDouble()).toInt())
            }

        }catch (e: Exception){
            e.printStackTrace()
            Log.d("Target", "exception: " + e.message)
        }

    }

    override fun getItemCount(): Int {
        return targets.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val tvTitle: TextView
        internal val targetedAmount: TextView
        internal val completedAmount: TextView
        internal val progressView: LinearProgressIndicator

        init {
            tvTitle = itemView.findViewById(R.id.tvTitle)
            targetedAmount = itemView.findViewById(R.id.targetedAmount)
            completedAmount= itemView.findViewById(R.id.completeAmount)
            progressView = itemView.findViewById(R.id.progressBar)
        }
    }
}