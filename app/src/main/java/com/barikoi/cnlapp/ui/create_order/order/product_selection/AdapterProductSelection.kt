package com.barikoi.cnlapp.ui.create_order.order.product_selection

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.data.remote.models.product.Product
import com.barikoi.cnlapp.databinding.ItemProductViewBinding
import java.util.Locale

class AdapterProductSelection(
    private val incrementListener: (Product, Int) -> Unit,
    private val decrementListener: (Product, Int) -> Unit
) : RecyclerView.Adapter<AdapterProductSelection.ProductViewHolder>(), Filterable {

    init {
        setHasStableIds(true)
    }

    var productList: List<Product> = emptyList()
    var filterProductList: List<Product> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductViewHolder {
        return ProductViewHolder(
            ItemProductViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: ProductViewHolder,
        position: Int
    ) {
        holder.binding.tvProductName.text = filterProductList[position].productName
        holder.binding.tvSubTotal.text = filterProductList[position].discountedUnitPrice.toString()
        holder.binding.tvCount.setText(
            filterProductList[position].qty.toString()
        )

        holder.binding.tvStockAvailable.text = holder.binding.tvStockAvailable.context.getString(
            R.string.in_stock,
            (filterProductList[position].currentAvailableStock - filterProductList[position].qty).toString()
        )


        holder.binding.btnPlus.setOnClickListener {
            incrementListener(filterProductList[position], position)
        }
        holder.binding.btnMinus.setOnClickListener {
            decrementListener(filterProductList[position], position)
        }
    }

    override fun getItemCount(): Int = filterProductList.size

    @SuppressLint("NotifyDataSetChanged")
    fun setProducts(newProducts: List<Product>) {
        this.productList = newProducts
        filterProductList = newProducts
        notifyDataSetChanged()
    }

    fun updateProductQuantity(product: Product, position: Int) {
        val updatedList = filterProductList.toMutableList()
        updatedList[position] = product
        filterProductList = updatedList
        notifyItemChanged(position, product)
    }

    override fun getItemId(position: Int): Long {
        return filterProductList[position].id.toLong() // Or any unique long value per item
    }

    inner class ProductViewHolder(val binding: ItemProductViewBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList: ArrayList<Product> = ArrayList()

                if (constraint.isNullOrEmpty()) {
                    filteredList.addAll(productList)
                } else {
                    val query = constraint.toString().trim().lowercase(Locale.ROOT)
                    productList.forEach {
                        if (it.productName.lowercase(Locale.ROOT).contains(query)) {
                            filteredList.add(it)
                        }
                    }
                }

                val results = FilterResults()
                results.values = filteredList
                return results
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence, results: FilterResults?) {
                if (results?.values is ArrayList<*>) {
                    filterProductList = emptyList()
                    filterProductList = results.values as List<Product>
                    notifyDataSetChanged()
                }
            }
        }
    }
}