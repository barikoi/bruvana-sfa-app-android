package com.barikoi.cnlapp.Adapter.so_view

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.Model.Products
import com.barikoi.cnlapp.Model.Shops
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.callback.OnSelectListener
import com.barikoi.cnlapp.callback.OnValueChangeListener
import java.text.DecimalFormat
import java.util.*

class ProductListAdapter(var mValues: List<Products>, mListener: OnValueChangeListener): RecyclerView.Adapter<ProductListAdapter.ViewHolder>(),
Filterable{

    var productList: List<Products> = mValues
    var mListener: OnValueChangeListener = mListener
    var dformat = DecimalFormat("#.##")


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductListAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.single_product_select_view, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ProductListAdapter.ViewHolder, position: Int) {
        holder.productName.text = productList[position].product_name
        holder.productUnit.text = productList[position].unit_name
        holder.perUnitPrice.text = productList[position].unit_price.toString()

        holder.layoutAdd.setOnClickListener {
            holder.layoutQty.visibility = View.VISIBLE
            holder.layoutAdd.visibility = View.GONE
        }

        if (productList[position].stock_available > 0){
            holder.stockAvailable.text = productList[position].stock_available.toString()+ " in stock"
        }else{
            holder.stockAvailable.text = "Stock out"
        }

        holder.perUnitPrice.text = productList[position].unit_price.toString()

        holder.productUnit.text = productList[position].unit_name

        /*try {
            val subtotal = productList[position].unit_price * holder.productCount.text.toString().toInt()
            holder.tvSubtoal.text = subtotal.toString()
        }catch (e:Exception){
            e.printStackTrace()
        }*/
        holder.productCount.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val subtotal = productList[position].unit_price * holder.productCount.text.toString().toInt()
                holder.tvSubtoal.text = dformat.format(subtotal).toString()
                mListener.onValueChanged(productList[position])

                if (holder.productCount.text.toString().toInt() == 0 || holder.productCount.text.toString().toInt() < 0){
                    holder.layoutQty.visibility = View.GONE
                    holder.layoutAdd.visibility = View.VISIBLE
                    holder.tvSubtoal.text = "0"
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        holder.tvAdd.setOnClickListener {
            val qtyValue = holder.productCount.text.toString().toInt() + 1
            holder.productCount.setText(qtyValue.toString())
            /*val subtotal = productList[position].unit_price * holder.productCount.text.toString().toInt()
            holder.tvSubtoal.text = subtotal.toString()*/
        }

        holder.tvMinus.setOnClickListener {
            val qtyValue = holder.productCount.text.toString().toInt() - 1
            holder.productCount.setText(qtyValue.toString())
            /*val subtotal = productList[position].unit_price * holder.productCount.text.toString().toInt()
            holder.tvSubtoal.text = subtotal.toString()*/
        }







    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val charString = constraint.toString()
                val filteredList: ArrayList<Products> = ArrayList<Products>()
                if (charString.isEmpty()) {
                    productList = mValues
                } else {
                    //val filteredList: ArrayList<RetailShops> = ArrayList<RetailShops>()
                    for (row in productList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.product_name.toLowerCase()
                                .contains(charString.lowercase(Locale.getDefault())) || row.product_name
                                .contains(charString)
                        ) {
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
                productList = results.values as List<Products>
                notifyDataSetChanged()
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val productName: TextView
        internal val stockAvailable: TextView
        internal val productUnit : TextView
        internal val perUnitPrice : TextView
        internal val tvSubtoal : TextView
        internal val tvMinus : TextView
        internal val tvAdd : TextView
        internal val productCount: EditText
        internal val layoutQty : LinearLayout
        internal val layoutAdd : LinearLayout

        init {
            productName = itemView.findViewById(R.id.tvProductName)
            stockAvailable = itemView.findViewById(R.id.tvStockAvailable)
            productUnit = itemView.findViewById(R.id.tvProductVariation)
            perUnitPrice = itemView.findViewById(R.id.tvPerUnit)
            tvSubtoal = itemView.findViewById(R.id.tvTotalPrice)
            tvMinus = itemView.findViewById(R.id.tvMinus)
            tvAdd = itemView.findViewById(R.id.tvAdd)
            productCount = itemView.findViewById(R.id.tvCount)
            layoutQty = itemView.findViewById(R.id.layoutQty)
            layoutAdd = itemView.findViewById(R.id.layoutAdd)



        }
    }
}