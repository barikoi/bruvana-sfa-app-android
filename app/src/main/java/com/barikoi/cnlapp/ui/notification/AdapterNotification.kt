package com.barikoi.cnlapp.ui.notification

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.data.remote.models.Notification
import com.barikoi.cnlapp.databinding.ItemNotificationBinding
import com.barikoi.cnlapp.utils.extension.formatHumanReadableDate
import com.barikoi.cnlapp.utils.extension.setHapticClickListener

class AdapterNotification(
    private val onItemClicked: (Notification) -> Unit
) : Adapter<AdapterNotification.NotificationViewHolder>() {

    private var notifications: MutableList<Notification> = mutableListOf()

    inner class NotificationViewHolder(val binding: ItemNotificationBinding) :
        ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        return NotificationViewHolder(
            ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = notifications.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateNotifications(notifications: List<Notification>) {
        this.notifications.clear()
        this.notifications.addAll(notifications)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addNotification(notification: Notification) {
        notifications.add(0, notification)
        notifyItemInserted(0)
    }


    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.binding.tvTitle.text = notifications[position].title
        holder.binding.tvMessage.text = notifications[position].message
        holder.binding.tvTime.text = notifications[position].createdAt.formatHumanReadableDate()

        if (notifications[position].readAt == null) {
            holder.binding.viewLine.isVisible = true

            holder.binding.llMain.background = AppCompatResources.getDrawable(
                holder.binding.root.context,
                R.color.green_color_light
            )
        } else {
            holder.binding.viewLine.isVisible = false

            holder.binding.llMain.background = AppCompatResources.getDrawable(
                holder.binding.root.context,
                R.color.white
            )
        }


        holder.binding.root.setHapticClickListener {
            onItemClicked(notifications[position])
        }
    }
}