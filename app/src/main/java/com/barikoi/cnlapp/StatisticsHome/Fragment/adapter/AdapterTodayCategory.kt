package com.barikoi.cnlapp.StatisticsHome.Fragment.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.StatisticsHome.Model.Categories
import com.barikoi.cnlapp.databinding.ItemTodayCategoryBinding

class AdapterTodayCategory : RecyclerView.Adapter<AdapterTodayCategory.TodayCategoryViewHolder>() {
    private var categories: List<Categories> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TodayCategoryViewHolder {
        val binding = ItemTodayCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TodayCategoryViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: TodayCategoryViewHolder,
        position: Int
    ) {
        val category = categories[position]
        holder.binding.apply {
            tvSerial.text = "#${(position + 1)}"
            tvShopName.text = category.outletCategory
            tvCompletedValue.text = category.orderDone
            tvTotalValue.text = " / ${category.totalOutlet}"
            tvTotalOrderValue.text = category.orderValue
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateCategories(newCategories: List<Categories>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = categories.size

    inner class TodayCategoryViewHolder(val binding: ItemTodayCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)
}