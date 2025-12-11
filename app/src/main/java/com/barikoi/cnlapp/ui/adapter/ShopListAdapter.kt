package com.barikoi.cnlapp.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.data.remote.models.Outlet
import com.barikoi.cnlapp.databinding.SingleShopListBinding
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.Constants
import com.barikoi.cnlapp.utils.SharePrefUtils
import java.util.Locale

class ShopListAdapter(
    private val isFromVisit: Boolean = false,
    private val onEditClickListener: (Outlet) -> Unit,
    private val onNavigationRouteClick: (Outlet) -> Unit,
    private val onVisitClickListener: (Outlet) -> Unit,
    private val sharePrefUtils: SharePrefUtils
) : RecyclerView.Adapter<ShopListAdapter.ShopViewHolder>(), Filterable {
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    var unFilteredShop: List<Outlet> = emptyList()
    var shopList: List<Outlet> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        return ShopViewHolder(
            SingleShopListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        holder.binding.shopName.text = shopList[position].outletName
        holder.binding.address.text = shopList[position].address
        holder.binding.shopState.isVisible = shopList[position].outletStatus.equals("active", true)
        if (shopList[position].id.toString() != "null") holder.binding.shopCode.text =
            shopList[position].id.toString()

        holder.binding.shopType.text = shopList[position].outletType
        holder.binding.territoryName.text = shopList[position].territoryName

        if (shopList[position].isVerified == 0) {
            holder.binding.imgNewTag.visibility = View.VISIBLE
        } else {
            holder.binding.imgNewTag.visibility = View.GONE
        }

        if (sharePrefUtils.getString(Api.USER_TYPE) == "SO") {
            holder.binding.btnVisit.isVisible = false
        } else {
            holder.binding.btnVisit.isVisible = true
        }

        holder.binding.btnVisit.isVisible = isFromVisit

        holder.binding.btnEdit.isVisible = !isFromVisit
        holder.binding.tvStatus.isVisible = !isFromVisit

        if (isFromVisit && shopList[position].visitStatus == "visited") {
            holder.binding.btnVisit.isEnabled = false
            holder.binding.btnVisit.text =
                holder.binding.btnVisit.context.getString(R.string.visited)
            holder.binding.btnVisit.backgroundTintList = ContextCompat.getColorStateList(
                holder.binding.btnVisit.context,
                R.color.light_yellow
            )

        } else {
            holder.binding.btnVisit.isEnabled = true
            holder.binding.btnVisit.text =
                holder.binding.btnVisit.context.getString(R.string.visit)
            holder.binding.btnVisit.backgroundTintList = ContextCompat.getColorStateList(
                holder.binding.btnVisit.context,
                R.color.colorVisited
            )
        }

        when (shopList[position].isVerified) {
            0 -> {
                holder.binding.tvStatus.text =
                    ContextCompat.getString(holder.binding.tvStatus.context, R.string.authorize_)
                holder.binding.tvStatus.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.authorize_text_color
                    )
                )
                holder.binding.tvStatus.background = ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.shape_order_status
                )
                holder.binding.tvStatus.backgroundTintList =
                    ContextCompat.getColorStateList(holder.itemView.context, R.color.authorize_bg)

                val drawable = ContextCompat.getDrawable(
                    holder.binding.tvStatus.context,
                    R.drawable.outline_verified_off_24
                )
                holder.binding.tvStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    drawable,
                    null,
                    null,
                    null
                )
            }

            1 -> {
                holder.binding.tvStatus.text =
                    ContextCompat.getString(holder.binding.tvStatus.context, R.string.verified)
                holder.binding.tvStatus.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.verified_text_color
                    )
                )

                holder.binding.tvStatus.background = ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.shape_order_status
                )
                holder.binding.tvStatus.backgroundTintList =
                    ContextCompat.getColorStateList(holder.itemView.context, R.color.verified_bg)

                val drawable = ContextCompat.getDrawable(
                    holder.binding.tvStatus.context,
                    R.drawable.outline_verified_24
                )
                holder.binding.tvStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    drawable,
                    null,
                    null,
                    null
                )
            }

            2 -> {
                holder.binding.tvStatus.text =
                    ContextCompat.getString(holder.binding.tvStatus.context, R.string.rejected_)
                holder.binding.tvStatus.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.rejected_text_color
                    )
                )
                holder.binding.tvStatus.background = ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.shape_order_status
                )
                holder.binding.tvStatus.backgroundTintList =
                    ContextCompat.getColorStateList(holder.itemView.context, R.color.rejected_bg)


                val drawable = ContextCompat.getDrawable(
                    holder.binding.tvStatus.context,
                    R.drawable.outline_cancel_24
                )
                holder.binding.tvStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    drawable,
                    null,
                    null,
                    null
                )
            }
        }

        holder.binding.tvDistance.text = String.format(
            Locale.ENGLISH, "%.2f m",
            shopList[position].distance
        )

        holder.binding.btnEdit.setOnClickListener {
            onEditClickListener(shopList[position])
        }

        holder.binding.ivRoute.setOnClickListener {
            onNavigationRouteClick(shopList[position])
        }

        holder.binding.ivRoute.setOnClickListener {
            onNavigationRouteClick(shopList[position])
        }
        holder.binding.btnVisit.setOnClickListener {
            onVisitClickListener(shopList[position])
        }
    }

    override fun getItemCount(): Int {
        return shopList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateShopList(shops: List<Outlet>) {
        this.shopList = shops
        this.unFilteredShop = shops
        notifyDataSetChanged()
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
    }

    inner class ShopViewHolder(val binding: SingleShopListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val charString = constraint.toString().trim()
                val filteredList: ArrayList<Outlet> = ArrayList()
                if (charString.isEmpty()) {
//                    shopList = unFilteredShop
                } else {
                    val query = charString.lowercase(Locale.ROOT)
                    for (row in unFilteredShop) {
                        // Check outlet name
                        if (row.outletName.lowercase(Locale.ROOT).contains(query)) {
                            filteredList.add(row)
                        }
                        // Check outlet ID (as string)
                        else if (row.id.toString().contains(charString)) {
                            filteredList.add(row)
                        }
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                filterResults.values = filteredList
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                shopList = results.values as List<Outlet>
                notifyDataSetChanged()
            }
        }
    }
}