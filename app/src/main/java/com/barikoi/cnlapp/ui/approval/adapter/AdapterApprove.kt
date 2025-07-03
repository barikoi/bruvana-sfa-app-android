package com.barikoi.cnlapp.ui.approval.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.barikoi.cnlapp.databinding.ItemProductBinding
import com.barikoi.cnlapp.utils.extension.performTapHaptic

class AdapterApprove(
    private val onCheckClicked: (Int) -> Unit,
    private val onTextChange: (String, Int) -> Unit
) : Adapter<AdapterApprove.ApproveViewHolder>() {

    private var requests: List<StockRequest> = emptyList()

    inner class ApproveViewHolder(val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApproveViewHolder {
        return ApproveViewHolder(
            ItemProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = requests.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateApproveData(requests: List<StockRequest>) {
        this.requests = requests
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ApproveViewHolder, position: Int) {

        holder.binding.tvProductName.text = requests[position].name
        holder.binding.cbSelect.isChecked = requests[position].isSelect
        holder.binding.etStockCount.setText(requests[position].stock.toString())

        holder.binding.cbSelect.setOnClickListener {
            it.performTapHaptic()
            onCheckClicked(position)
        }

        holder.binding.etStockCount.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrEmpty()) {
                onTextChange(text.toString(), position)
            }
        }

    }
}

data class StockRequest(
    val id: Int,
    val dbHouseId: Int,
    val name: String,
    var stock: Int = 0,
    var isSelect: Boolean = true
)
