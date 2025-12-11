package com.barikoi.cnlapp.order_create.Adapter

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.Model.Products
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.RoomDb.AppDatabase
import com.barikoi.cnlapp.order_create.Callback.OnValueChangeListener
import com.barikoi.cnlapp.order_create.RoomDB.SaveOrder
import com.barikoi.cnlapp.utils.Api
import java.text.DecimalFormat
import java.util.Locale

class ProductListAdapter(var mValues: List<Products>, var mListener: OnValueChangeListener) :
    RecyclerView.Adapter<ProductListAdapter.ViewHolder>(),
    Filterable {

    var productList: List<Products> = mValues
    var dformat = DecimalFormat("#.##")
    var totalPrice = 0.0
    lateinit var mRecyclerView: RecyclerView
    private var prefs: SharedPreferences? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.single_product_view, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.setIsRecyclable(false)
        val appDatabase = AppDatabase.getInstance(holder.itemView.context)
        prefs = PreferenceManager.getDefaultSharedPreferences(holder.itemView.context)

        holder.stockAvailable.visibility = View.VISIBLE
        holder.productName.text = productList[position].productName
        holder.productUnit.text = productList[position].unitName
        holder.perUnitPrice.text = productList[position].discountedUnitPrice.toString()

        if (productList[position].orderedQuantity > 0) {
            holder.productCount.setText(productList[position].orderedQuantity.toString())
            holder.btnMinus.drawable.setTint(holder.itemView.resources.getColor(R.color.cnl_color_2))
        } else {
            holder.btnMinus.drawable.setTint(holder.itemView.resources.getColor(R.color.btn_gray_stroke))
        }

        if (productList[position].orderedTotalPrice > 0.0) {
            holder.tvSubtoal.text = dformat.format(productList[position].orderedTotalPrice).toString()
        }

        if (productList[position].stockAvailable > 0) {
            holder.stockAvailable.text =
                productList[position].stockAvailable.toString() + " in stock"
        } else {
            holder.stockAvailable.text = holder.itemView.resources.getString(R.string.stock_out)
        }

        holder.perUnitPrice.text = productList[position].discountedUnitPrice.toString()

        holder.productUnit.text = productList[position].unitName

        holder.btnAdd.drawable.setTint(holder.itemView.resources.getColor(R.color.cnl_color_2))

        holder.btnAdd.setOnClickListener {
            if (productList[position].stockAvailable > 0) {
                val qtyValue = holder.productCount.text.toString().toInt() + 1
                holder.productCount.setText(qtyValue.toString())
                productList[position].orderedQuantity = holder.productCount.text.toString().toInt()
                productList[position].orderedTotalPrice =
                    productList[position].discountedUnitPrice * holder.productCount.text.toString()
                        .toInt()
                productList[position].stockAvailable = productList[position].stockAvailable - 1
                holder.stockAvailable.text =
                    productList[position].stockAvailable.toString() + " in stock"
                val prodList = appDatabase!!.saveOrderDao()
                    .getOrdersDB(prefs!!.getString(Api.SELECTED_SHOP_ID, "")!!)
                try {
                    if (prodList!!.size > 0) {
                        Log.d(
                            "Product",
                            "item count add: " + prodList[0].itemsCount + " shopId: " + prodList[0].outletId + " Total Price: " + prodList[0].totalPrice
                        )
                        appDatabase.saveOrderDao().update(
                            prefs!!.getString(Api.SELECTED_SHOP_ID, "")!!,
                            prodList[0].itemsCount + 1,
                            prodList[0].totalPrice + productList[position].discountedUnitPrice
                        )
                    } else {
                        appDatabase.saveOrderDao().insertAll(
                            SaveOrder(
                                null,
                                prefs!!.getString(Api.SELECTED_SHOP_ID, "")!!,
                                holder.productCount.text.toString().toInt(),
                                totalPrice
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.d("Product", "exception: " + e.message)
                }


                mListener.onValueChanged(productList[position], position)
            } else {
                Toast.makeText(
                    holder.itemView.context,
                    holder.itemView.resources.getString(R.string.stock_out),
                    Toast.LENGTH_SHORT
                ).show()
                holder.stockAvailable.text = holder.itemView.resources.getString(R.string.stock_out)
            }

        }

        holder.btnMinus.setOnClickListener {
            if (holder.productCount.text.toString()
                    .toInt() > 0
            ) {
                val qtyValue = holder.productCount.text.toString().toInt() - 1
                holder.productCount.setText(qtyValue.toString())
                productList[position].orderedQuantity = holder.productCount.text.toString().toInt()
                productList[position].orderedTotalPrice =
                    productList[position].discountedUnitPrice * holder.productCount.text.toString()
                        .toInt()
                productList[position].stockAvailable = productList[position].stockAvailable + 1
                holder.stockAvailable.text =
                    productList[position].stockAvailable.toString() + " in stock"
                val prodList = appDatabase!!.saveOrderDao()
                    .getOrdersDB(prefs!!.getString(Api.SELECTED_SHOP_ID, "")!!)
                if (prodList!!.isNotEmpty()) {
                    Log.d(
                        "Product",
                        "item count minus: " + prodList[0].itemsCount + " shopId: " + prodList[0].outletId
                    )
                    appDatabase.saveOrderDao().update(
                        prefs!!.getString(Api.SELECTED_SHOP_ID, "")!!,
                        prodList[0].itemsCount - 1,
                        prodList[0].totalPrice - productList[position].discountedUnitPrice
                    )
                } else {
                    appDatabase.saveOrderDao().insertAll(
                        SaveOrder(
                            null,
                            prefs!!.getString(Api.SELECTED_SHOP_ID, "")!!,
                            holder.productCount.text.toString().toInt(),
                            totalPrice
                        )
                    )
                }

                mListener.onValueChanged(productList[position], position)
            }
        }

        holder.productCount.isEnabled = false
        holder.productCount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (holder.productCount.text.toString().trim().isNotEmpty()) {
                    val subtotal =
                        productList[position].discountedUnitPrice * holder.productCount.text.toString()
                            .toInt()
                    holder.tvSubtoal.text = dformat.format(subtotal).toString()
                    totalPrice = subtotal

                    productList[position].orderedTotalPrice = subtotal
                    productList[position].orderedQuantity =
                        holder.productCount.text.toString().toInt()

                    if (holder.productCount.text.toString()
                            .toInt() == 0 || holder.productCount.text.toString().toInt() < 0
                    ) {
                        holder.btnMinus.isEnabled = false
                        totalPrice = 0.0
                        holder.tvSubtoal.text = "0"
                        holder.btnMinus.drawable.setTint(holder.itemView.resources.getColor(R.color.btn_gray_stroke))
                    } else {
                        holder.btnMinus.drawable.setTint(holder.itemView.resources.getColor(R.color.cnl_color_2))
                        holder.btnMinus.isEnabled = true
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {}

        })
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        Log.d("Product", "view holder: " + recyclerView.childCount)
        this.mRecyclerView = recyclerView
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
                    for (row in mValues) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.productName.toLowerCase()
                                .contains(charString.lowercase(Locale.getDefault())) || row.productName
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
        internal val productName: TextView = itemView.findViewById(R.id.productName)
        internal val stockAvailable: TextView = itemView.findViewById(R.id.tvStockAvailable)
        internal val productUnit: TextView = itemView.findViewById(R.id.tvProductVariation)
        internal val perUnitPrice: TextView = itemView.findViewById(R.id.tvPerUnit)
        internal val tvSubtoal: TextView = itemView.findViewById(R.id.tvSubTotal)
        internal val btnMinus: ImageButton = itemView.findViewById(R.id.btnminus)
        internal val btnAdd: ImageButton = itemView.findViewById(R.id.btnPlus)
        internal val productCount: EditText = itemView.findViewById(R.id.tvCount)
    }
}