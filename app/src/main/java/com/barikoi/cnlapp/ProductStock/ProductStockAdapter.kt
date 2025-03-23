package com.barikoi.cnlapp.ProductStock

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.data.remote.models.Product
import com.barikoi.cnlapp.databinding.ProductViewStockBinding
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.extension.englishToBanglaNumber
import com.barikoi.cnlapp.utils.extension.performTapHaptic
import com.bumptech.glide.Glide


class ProductStockAdapter(
    private val isSummaryActivity: Boolean?,
    private val isTO: Boolean?,
    private val onItemLongPressed: (Int) -> Unit,
    private val onItemClick: (Int) -> Unit,
    private val onCBClicked: (Int) -> Unit,
    private val onTextChange: (String, Int) -> Unit
) :
    RecyclerView.Adapter<ProductStockAdapter.ViewHolder>() {

    var products: List<Product> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ProductViewStockBinding.bind(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.product_view_stock, parent, false)
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        val mItem = products[position]

        holder.binding.productName.text = mItem.productName
        if (isSummaryActivity == true) {
            holder.binding.cbSelect.visibility = View.GONE
            holder.binding.etPerUnitSold.visibility = View.GONE
            holder.binding.tvPerUnitSold.visibility = View.VISIBLE
            holder.binding.tvPerUnitSold.text = mItem.deliveredQuantity.toString()
            holder.binding.tvPerUnit.text = mItem.unitName
            holder.binding.tvAmount.text = mItem.deliveredAmount.toString()

            holder.binding.tvSoldQuantity.text =
                "${holder.itemView.context.resources.getString(R.string.sold_in)} ${mItem.productiveRoutes} ${
                    holder.itemView.context.resources.getString(
                        R.string.route
                    )
                }"

        } else if (isTO == true) {
            holder.binding.cbSelect.visibility = View.GONE
            holder.binding.etPerUnitSold.visibility = View.VISIBLE
            holder.binding.tvPerUnitSold.visibility = View.GONE
            holder.binding.tvPerUnitSold.text = mItem.deliveredQuantity.toString()

            holder.binding.tvAmount.text =
                (mItem.unitPrice.toDouble() * mItem.currentAvailableStock).toString()

            holder.binding.tvPerUnit.text = mItem.unitName
            holder.binding.tvSoldQuantity.text =
                "${mItem.productiveOutlets.toString().englishToBanglaNumber()} ${
                    holder.itemView.context.getString(
                        R.string.shops_ordered_this_month
                    )
                }"
        } else {
            holder.binding.cbSelect.visibility = View.VISIBLE
            holder.binding.tvPerUnitSold.visibility = View.GONE
            holder.binding.tvPerUnit.text = mItem.unitName

            holder.binding.tvAmount.text =
                (mItem.unitPrice.toDouble() * mItem.currentAvailableStock).toString()

            holder.binding.tvSoldQuantity.text =
                "${mItem.productiveOutlets.toString().englishToBanglaNumber()} ${
                    holder.itemView.context.getString(
                        R.string.shops_ordered_this_month
                    )
                }"
        }

        if (products.any { it.isSelect }) {
            holder.binding.etPerUnitSold.setText(
                mItem.stockValue
            )
        } else {
            holder.binding.etPerUnitSold.setText(
                mItem.currentAvailableStock.toString()
            )
        }


        if (mItem.isSelect) {
            holder.binding.etPerUnitSold.isEnabled = true
            holder.binding.cbSelect.isChecked = true
            holder.binding.llMain.background.setTint(
                ContextCompat.getColor(
                    holder.binding.imageProduct.context,
                    R.color.card_selected_bg
                )
            )
        } else {
            holder.binding.llMain.background.setTint(
                ContextCompat.getColor(
                    holder.binding.imageProduct.context,
                    R.color.white
                )
            )
            holder.binding.etPerUnitSold.isEnabled = false
            holder.binding.cbSelect.isChecked = false
        }


        val drawable = CircularProgressDrawable(holder.itemView.context)
        drawable.setColorSchemeColors(
            holder.itemView.context.resources.getColor(R.color.cnl_color_1),
            holder.itemView.context.resources.getColor(R.color.cnl_color_2)
        )
        drawable.centerRadius = 20f
        drawable.strokeWidth = 6f
        drawable.start()

        if (mItem.images.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(mItem.images[0].imageUrl)
                .placeholder(drawable)
                .into(holder.binding.imageProduct)
        } else {
            holder.binding.imageProduct.visibility = View.GONE
        }

        holder.binding.etPerUnitSold.doOnTextChanged { text, _, _, _ ->
            if (text.toString().isNotEmpty() && text.toString().toInt() < 1000) {
                onTextChange(text.toString(), position)
            } else if (text.toString().isNotEmpty() && text.toString().toInt() > 999) {
                Toast.makeText(
                    holder.itemView.context,
                    "Stock request must be less than 1000",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        holder.binding.root.setOnLongClickListener {
            it.performTapHaptic()
            onItemLongPressed(position)
            true
        }

        holder.binding.cbSelect.setOnClickListener {
            it.performTapHaptic()
            onCBClicked(position)
        }

        holder.binding.root.setOnClickListener {
            onItemClick(position)
        }

    }

    override fun getItemCount(): Int = products.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateProducts(products: List<Product>) {
        this.products = products
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ProductViewStockBinding) :
        RecyclerView.ViewHolder(binding.root)
}