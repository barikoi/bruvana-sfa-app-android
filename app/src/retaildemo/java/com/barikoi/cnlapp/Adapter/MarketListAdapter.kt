package com.barikoi.cnlapp.Adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.Model.Markets
import com.barikoi.cnlapp.Model.Routes
import com.barikoi.cnlapp.R
import kotlinx.android.synthetic.main.fragment_product_select.*
import org.w3c.dom.Text

class MarketListAdapter(val routes: List<Markets>) : RecyclerView.Adapter<MarketListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.single_route_list, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemCount.text = (position+1).toString()
        holder.routeName.text = routes[position].route_name
        holder.territoryName.text = routes[position].territory_name
        holder.areaName.text = routes[position].area_name
        holder.routeCode.text = routes[position].route_code
        holder.shopCount.text = routes[position].outlet_count

        val gd = GradientDrawable()
        gd.setColor(holder.itemView.resources.getColor(R.color.white))
        gd.cornerRadius = 5f
        gd.setStroke(2, holder.itemView.resources.getColor(R.color.cnl_color_1))
        holder.itemCount.setBackgroundDrawable(gd)
    }

    override fun getItemCount(): Int {
        return routes.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val itemCount: TextView
        internal val routeName: TextView
        internal val territoryName: TextView
        internal val areaName: TextView
        internal val routeCode: TextView
        internal val shopCount: TextView
        init {
            itemCount = itemView.findViewById(R.id.item_count_id)
            routeName = itemView.findViewById(R.id.route_name)
            territoryName = itemView.findViewById(R.id.territory_name)
            areaName = itemView.findViewById(R.id.area_name)
            routeCode = itemView.findViewById(R.id.route_code)
            shopCount = itemView.findViewById(R.id.shop_count)

        }
    }
}