package com.barikoi.cnlapp.Order_Create.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.Model.Shops
import com.barikoi.cnlapp.Order_Create.Callback.DialogListener
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Order_Create.Callback.OnSelectListener
import com.barikoi.cnlapp.Utils.ViewUtils
import java.text.SimpleDateFormat
import java.util.*

class ShopSelectAdapter(var mValues: List<Shops>, mListener: OnSelectListener): RecyclerView.Adapter<ShopSelectAdapter.ViewHolder>(),
    Filterable {

    var shopList: List<Shops> = mValues
    var mListener: OnSelectListener = mListener


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.single_outlet_satistics, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.background = holder.itemView.resources.getDrawable(R.drawable.cardview_bg)
        holder.btnDetails.text = holder.itemView.resources.getString(R.string.select)
        holder.ownerName.visibility = View.VISIBLE

        holder.setIsRecyclable(false)

        holder.shopName.text= shopList[position].shop_name
        holder.ownerName.text = shopList[position].shop_owner
        if (!shopList[position].lastOrderDate.equals("null")){
            val oldDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val df = SimpleDateFormat("dd LLL yyyy", Locale.ENGLISH)
            val orderDate = df.format(oldDate.parse(shopList[position].lastOrderDate))
            holder.lastOrderDate.text = holder.itemView.context.resources.getString(com.barikoi.cnlapp.R.string.last_order_date)+orderDate
        }
        if (shopList[position].category.length > 0 && !shopList[position].category.equals("null", true)) {
            holder.tvCategory.text =
                shopList[position].category.get(0).toString().uppercase(Locale.getDefault())
        }

        if (shopList[position].isOrdered == 1){
            holder.isOrdered.setImageResource(R.drawable.ic_ordered)
            holder.isOrdered.visibility = View.VISIBLE
        }else if (shopList[position].isNoOrdered == 1){
            holder.isOrdered.setImageResource(R.drawable.ic_no_ordered)
            holder.isOrdered.visibility = View.VISIBLE
        } else{
            holder.isOrdered.visibility = View.GONE
        }
        /*if (!shopList[position].imageUrl.isNullOrEmpty() && !shopList[position].imageUrl.equals("null")){
            Glide.with(holder.itemView.context)
                .load(shopList[position].imageUrl)
                .error(R.drawable.shop)
                .into(holder.imageShop)
        }else{
            //holder.imageProduct.visibility = View.INVISIBLE
        }*/


        holder.btnDetails.setOnClickListener {
            if (shopList[position].isOrdered == 1 || shopList[position].isNoOrdered == 1){
                ViewUtils.viewDialogResponse(holder.itemView.context, "Already visited this outlet for today", object : DialogListener{
                    override fun onConfirmed() {

                    }

                    override fun onCanceled() {

                    }

                })
            }else{
                mListener.onShopSelected(shopList[position])
            }

        }
    }

    override fun getItemCount(): Int {
        return shopList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val charString = constraint.toString()
                val filteredList: ArrayList<Shops> = ArrayList<Shops>()
                if (charString.isEmpty()) {
                    shopList = mValues
                } else {
                    //val filteredList: ArrayList<RetailShops> = ArrayList<RetailShops>()
                    for (row in mValues) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.shop_name.toLowerCase().contains(charString.lowercase(Locale.getDefault()))) {
                            filteredList.add(row)
                        }
                    }
                    //itemList = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                shopList = results.values as List<Shops>
                notifyDataSetChanged()
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val shopName: TextView
        internal val ownerName: TextView
        internal val tvCategory: TextView
        internal val lastOrderDate: TextView
        internal val imageShop: ImageView
        internal val isOrdered: ImageView
        internal val btnDetails: AppCompatButton
        init {
            ownerName = itemView.findViewById(R.id.ownerName)
            tvCategory = itemView.findViewById(R.id.tvcategory)
            shopName = itemView.findViewById(R.id.shopName)
            imageShop = itemView.findViewById(R.id.imageShop)
            lastOrderDate = itemView.findViewById(R.id.orderDate)
            btnDetails = itemView.findViewById(R.id.btnDetails)
            isOrdered = itemView.findViewById(R.id.isOrderView)

        }
    }
}