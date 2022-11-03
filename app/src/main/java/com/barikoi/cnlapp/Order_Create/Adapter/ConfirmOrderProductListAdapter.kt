package com.barikoi.cnlapp.Order_Create.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.Model.Products
import com.barikoi.cnlapp.R

class ConfirmOrderProductListAdapter(var mValues: List<Products>):RecyclerView.Adapter<ConfirmOrderProductListAdapter.ViewHolder>() {

    var productList: List<Products> = mValues

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.single_confirm_order_product_listview, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.productName.text = productList[position].product_name
        holder.addedProduct.text = productList[position].ordered_quantity.toString()
        holder.tvSubtoal.text = productList[position].ordered_total_price.toString()
        holder.productCount.setText(productList[position].ordered_quantity.toString())

        holder.tvAdd.setOnClickListener {
            val qtyValue = holder.productCount.text.toString().toInt() + 1
            holder.productCount.setText(qtyValue.toString())
            holder.addedProduct.text = qtyValue.toString()
            /*val subtotal = productList[position].unit_price * holder.productCount.text.toString().toInt()
            holder.tvSubtoal.text = subtotal.toString()*/
        }

        holder.tvMinus.setOnClickListener {
            val qtyValue = holder.productCount.text.toString().toInt() - 1
            holder.productCount.setText(qtyValue.toString())
            holder.addedProduct.text = qtyValue.toString()
            /*val subtotal = productList[position].unit_price * holder.productCount.text.toString().toInt()
            holder.tvSubtoal.text = subtotal.toString()*/
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val productName: TextView
        internal val addedProduct: TextView
        internal val tvSubtoal : TextView
        internal val tvMinus : TextView
        internal val tvAdd : TextView
        internal val productCount: EditText
        /*internal val layoutQty : LinearLayout
        internal val layoutAdd : LinearLayout*/

        init {
            productName = itemView.findViewById(R.id.tvProductName)
            addedProduct = itemView.findViewById(R.id.tvAddedProduct)
            tvSubtoal = itemView.findViewById(R.id.tvTotalPrice)
            tvMinus = itemView.findViewById(R.id.tvMinus)
            tvAdd = itemView.findViewById(R.id.tvAdd)
            productCount = itemView.findViewById(R.id.tvCount)
            /*layoutQty = itemView.findViewById(R.id.layoutQty)
            layoutAdd = itemView.findViewById(R.id.layoutAdd)
*/


        }
    }
}