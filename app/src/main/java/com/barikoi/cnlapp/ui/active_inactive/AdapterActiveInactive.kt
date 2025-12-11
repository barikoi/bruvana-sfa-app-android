package com.barikoi.cnlapp.ui.active_inactive

import com.barikoi.cnlapp.R

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.barikoi.cnlapp.data.remote.models.ActiveInactiveUser
import com.barikoi.cnlapp.databinding.ItemActiveInactiveBinding
import com.barikoi.cnlapp.utils.extension.formateDateNewFormat

class AdapterActiveInactive(
    private val status: String
) : RecyclerView.Adapter<AdapterActiveInactive.ActiveInactiveViewHolder>() {
    private var activeInactiveList: List<ActiveInactiveUser> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ActiveInactiveViewHolder {
        return ActiveInactiveViewHolder(
            ItemActiveInactiveBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(
        holder: ActiveInactiveViewHolder, position: Int
    ) {
        val activeInactiveUser = activeInactiveList[position]

        holder.binding.tvName.text = activeInactiveUser.userName
        holder.binding.lastAddress.text = activeInactiveUser.checkinAddress ?: ""

        holder.binding.tvMonth.text =
            activeInactiveUser.updatedAt?.formateDateNewFormat("LLL") ?: "--:--"
        holder.binding.tvDate.text =
            activeInactiveUser.updatedAt?.formateDateNewFormat("dd") ?: "--:--"
        holder.binding.updatedTime.text =
            activeInactiveUser.updatedAt?.formateDateNewFormat("hh:mm a") ?: "--:--"


        holder.binding.imageUser.load(
            activeInactiveUser.image?.firstOrNull()?.imageUrl
        )

        if (status == "Active Users") {
            holder.binding.imageStatus.setImageDrawable(
                ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_active)
            )
        } else {
            holder.binding.imageStatus.setImageDrawable(
                ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_inactive)
            )
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<ActiveInactiveUser>) {
        activeInactiveList = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = activeInactiveList.size

    inner class ActiveInactiveViewHolder(val binding: ItemActiveInactiveBinding) :
        RecyclerView.ViewHolder(binding.root)
}