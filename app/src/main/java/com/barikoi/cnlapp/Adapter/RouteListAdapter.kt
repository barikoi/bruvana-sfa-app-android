package com.barikoi.cnlapp.Adapter

import android.annotation.SuppressLint
import android.icu.text.NumberFormat
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.barikoi.cnlapp.data.remote.models.Route
import com.barikoi.cnlapp.databinding.SingleRouteListBinding
import com.barikoi.cnlapp.utils.extension.englishToBanglaNumber
import java.util.Locale

class RouteListAdapter :
    RecyclerView.Adapter<RouteListAdapter.RouteViewHolder>() {

    private var routesList: List<Route> = emptyList()

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        holder.bindData(routesList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        return RouteViewHolder(
            SingleRouteListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setRouteListData(routesList: List<Route>) {
        this.routesList = routesList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = routesList.size

    inner class RouteViewHolder(private val binding: SingleRouteListBinding) :
        ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bindData(route: Route) {
            binding.itemCountId.text =
                (adapterPosition + 1).toString().englishToBanglaNumber()
            binding.routeName.text = route.routeName
            binding.territoryName.text = route.territoryName
            binding.areaName.text = route.areaName
            binding.routeCode.text = route.routeCode
            binding.shopCount.text = route.outletCount.englishToBanglaNumber()
        }
    }
}