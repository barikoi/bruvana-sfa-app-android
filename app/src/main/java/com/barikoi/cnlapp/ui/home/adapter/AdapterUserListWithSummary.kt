package com.barikoi.cnlapp.ui.home.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.data.remote.models.UserSummary
import com.barikoi.cnlapp.databinding.ItemUserBinding
import com.barikoi.cnlapp.utils.extension.setHapticClickListener

class AdapterUserListWithSummary(
    private val onUserClick: (UserSummary, Int) -> Unit
) :
    RecyclerView.Adapter<AdapterUserListWithSummary.UserListWithSummaryViewHolder>() {
    private var users: List<UserSummary> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserListWithSummaryViewHolder {
        return UserListWithSummaryViewHolder(
            ItemUserBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onBindViewHolder(
        holder: UserListWithSummaryViewHolder,
        position: Int
    ) {
        val user = users[position]

        with(holder.binding) {
            tvName.text = user.name + " (${user.userType})"

            if (user.userType == "TO") {
                tvActiveUser.text = user.active.toString()
                tvInactiveUser.text = user.inactive.toString()
            } else {
                tvActiveUser.isVisible = false
                tvInactiveUser.isVisible = false
            }
            tvOrderValue.text = String.format(
                "%,.2f",
                user.totalOrders
            )
            tvVisitValue.text = user.numberOfVisits.toString()
            tvMemosValue.text = user.numberOfMemos.toString()
        }

        holder.binding.root.setHapticClickListener {
            onUserClick(user, position)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newUsers: List<UserSummary>) {
        users = newUsers
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = users.size

    inner class UserListWithSummaryViewHolder(val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root)
}