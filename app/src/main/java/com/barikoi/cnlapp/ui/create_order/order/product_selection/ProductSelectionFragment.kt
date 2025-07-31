package com.barikoi.cnlapp.ui.create_order.order.product_selection

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.data.remote.models.Outlet
import com.barikoi.cnlapp.data.remote.models.ProductStatistics
import com.barikoi.cnlapp.data.remote.models.product.Product
import com.barikoi.cnlapp.databinding.FragmentProductSelectionBinding
import com.barikoi.cnlapp.order_create.Adapter.OutletProductAdapter
import com.barikoi.cnlapp.ui.add_gift.AddGiftActivity
import com.barikoi.cnlapp.ui.create_order.order.product_selection.vm.ProductSelectViewModel
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.extension.format
import com.barikoi.cnlapp.utils.extension.formateDate
import com.barikoi.cnlapp.utils.extension.formattedDateTime
import com.barikoi.cnlapp.utils.extension.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class ProductSelectionFragment(val viewModel: ProductSelectViewModel, val outlet: Outlet) :
    Fragment() {
    private lateinit var binding: FragmentProductSelectionBinding

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    private lateinit var adapterProductSelection: AdapterProductSelection

    private var updatedProductsList: MutableList<Product> = mutableListOf()

    private var giftData: String = ""

    private val addGiftResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                // Safely check if the data is not null before accessing it
                if (data != null) {
                    giftData = data.getStringExtra("gift_result") ?: ""

                    if (giftData.isNotEmpty()) {
                        // Update the button icon if the gift data is not empty
                        binding.btnAddGift.setIconResource(R.drawable.ic_edit_square)
                    }
                } else {
                    // Handle case where data is null (optional)
                    Log.e("MainActivity", "Received null data from result.")
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getProducts(
            sharePrefUtils.getString(Api.USER_ID)!!,
        )

        viewModel.getPreviousDayOrder(
            sharePrefUtils.getString(Api.USER_ID)!!,
            outlet.id.toString()
        )

        startProductObserve()
        startPreProductObserve()

        adapterProductSelection = AdapterProductSelection(
            incrementListener = { product, pos ->
                AppLogger.log("ProductSelectionFragment::incrementListener: ${product.productName}")
                if (product.currentAvailableStock > product.qty) {
                    val existingProduct = updatedProductsList.find { it.id == product.id }
                    if (existingProduct != null) {
                        // If the product already exists, increment its quantity
                        existingProduct.qty = existingProduct.qty + 1
                    } else {
                        // If the product does not exist, add it to the list with quantity 1
                        updatedProductsList.add(product.copy(qty = 1))
                    }
                } else {
                    // If the product is out of stock, show a message or handle accordingly
                    toast("Product is out of stock")
                    return@AdapterProductSelection
                }

                viewModel.setProducts(updatedProductsList)
                adapterProductSelection.updateProductQuantity(product, pos)

            },
            decrementListener = { product, pos ->
                AppLogger.log("ProductSelectionFragment::decrementListener: ${product.productName}")

                val existingProduct = updatedProductsList.find { it.id == product.id }
                if (existingProduct != null) {
                    // If the product exists, decrement its quantity
                    if (existingProduct.qty > 0) {
                        existingProduct.qty = existingProduct.qty - 1
                    } else {
                        return@AdapterProductSelection
                    }
                }

                viewModel.setProducts(updatedProductsList)
                adapterProductSelection.updateProductQuantity(product, pos)
            },
        )

        binding.editTextSearchProduct.doOnTextChanged { text, _, _, _ ->
            adapterProductSelection.filter.filter(text)
        }

        binding.rcvProducts.layoutManager = LinearLayoutManager(
            requireContext()
        )
        binding.rcvProducts.adapter = adapterProductSelection

        val itemAnimator =  binding.rcvProducts.itemAnimator
        if (itemAnimator is SimpleItemAnimator) {
            itemAnimator.supportsChangeAnimations = false
        }

        binding.btnAddGift.setOnClickListener {
            addGiftResultLauncher.launch(
                Intent(
                    requireActivity(),
                    AddGiftActivity::class.java
                ).apply {
                    putExtra("outlet_id", outlet.id.toString())
                    putExtra("shop_name", outlet.outletName)
                    if (giftData.isNotEmpty()) {
                        putExtra(
                            "gift_data", giftData
                        )
                    }
                }
            )
        }

        binding.tvSortTitle.setOnClickListener {
            val popup = PopupMenu(requireContext(), binding.tvSortTitle)
            popup.menuInflater.inflate(R.menu.sort_menu_product, popup.menu)
            popup.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener,
                PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    when (item.itemId) {
                        R.id.menu_ztoa -> {
                            updatedProductsList.sortByDescending {
                                it.productName
                            }
                            adapterProductSelection.setProducts(updatedProductsList)
                            binding.tvSortTitle.text = resources.getString(R.string.ztoa)
                        }

                        R.id.menu_atoz -> {
                            updatedProductsList.sortBy {
                                it.productName
                            }
                            adapterProductSelection.setProducts(updatedProductsList)

                            binding.tvSortTitle.text = resources.getString(R.string.atoz)
                        }

                        R.id.menu_mostfrequent -> {
                            updatedProductsList.sortByDescending {
                                it.quantityLastMonth
                            }
                            adapterProductSelection.setProducts(updatedProductsList)

                            binding.tvSortTitle.text = resources.getString(R.string.most_frequent)
                        }

                        R.id.menu_lowstock -> {
                            updatedProductsList.sortBy {
                                it.currentAvailableStock
                            }
                            adapterProductSelection.setProducts(updatedProductsList)
                            binding.tvSortTitle.text = resources.getString(R.string.low_stock)
                        }

                        R.id.menu_highstock -> {
                            updatedProductsList.sortByDescending {
                                it.currentAvailableStock
                            }
                            adapterProductSelection.setProducts(updatedProductsList)
                            binding.tvSortTitle.text = resources.getString(R.string.high_stock)
                        }
                    }
                    return true
                }
            })
            popup.show()
        }
    }

    private fun startPreProductObserve() {
        lifecycleScope.launch {
            viewModel.previousDayResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("startProductObserve::Empty")
                    }

                    is ApiState.Error -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("startProductObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        binding.progressBar.isVisible = true
                        AppLogger.log("startProductObserve::Loading")
                    }

                    is ApiState.Success -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("startProductObserve:: Success ${it.data}")

                        viewPreviousOrderDialog(
                            it.data!!.outlets.first()
                        )
                    }
                }
            }
        }
    }

    private fun startProductObserve() {
        lifecycleScope.launch {
            viewModel.productResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("startProductObserve::Empty")
                    }

                    is ApiState.Error -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("startProductObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        binding.progressBar.isVisible = true
                        AppLogger.log("startProductObserve::Loading")
                    }

                    is ApiState.Success -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("startProductObserve:: Success ${it.data}")

                        updatedProductsList = it.data!!.products.toMutableList()
                        adapterProductSelection.setProducts(updatedProductsList)

                        viewModel.setProducts(updatedProductsList)
                    }
                }
            }
        }
    }

    fun viewPreviousOrderDialog(
        outlet: com.barikoi.cnlapp.data.remote.models.pre_order.Outlet
    ) {
        val dialog = Dialog(requireContext())
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_previous_order_list)

        val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)

        val outletName = dialog.findViewById<TextView>(R.id.outletName)
        outletName.text = outlet.outletName

        val tvOutletCategory = dialog.findViewById<TextView>(R.id.tvCategory)
        tvOutletCategory.text = outlet.outletCategory ?: "N/A"

        val rcvProductList = dialog.findViewById<RecyclerView>(R.id.productList)

        val tvLastOrderDate = dialog.findViewById<TextView>(R.id.lastOrderDate)
        val tvLastDeliveryDate = dialog.findViewById<TextView>(R.id.lastDeliveryDate)
        val tvItemCount = dialog.findViewById<TextView>(R.id.itemCount)
        val tvGrandTotal = dialog.findViewById<TextView>(R.id.grandTotal)
        val tvOrderStatus = dialog.findViewById<TextView>(R.id.tvOrderStatus)
        val statusLayout = dialog.findViewById<LinearLayout>(R.id.layoutStatus)
        val filterLayout = dialog.findViewById<LinearLayout>(R.id.filterLayout)
        val filterTitle = dialog.findViewById<TextView>(R.id.filterTitle)
        val tvMinOrderValue = dialog.findViewById<TextView>(R.id.minOV)
        val startOrder = dialog.findViewById<AppCompatButton>(R.id.btnStartOrder)

        val latestOrder = outlet.orders.maxByOrNull { it.orderedAt }

        tvMinOrderValue.text = getString(R.string.min_order_value, outlet.minimumOrder ?: "N/A")

        latestOrder?.let {
            tvLastOrderDate.text = getString(
                R.string.last_order_date,
                it.orderedAt.formateDate()
            )
            if (it.deliveredAt != null)
                tvLastDeliveryDate.text =
                    getString(R.string.last_delivery_date, it.deliveredAt.formateDate())
        }

        startOrder.setOnClickListener {
            viewModel.setOrderStartTime(Calendar.getInstance().formattedDateTime())
            dialog.dismiss()
        }

        val orderStatus = latestOrder?.orderStatus ?: "PENDING"

        val productItems = mutableListOf<ProductStatistics>()

        fun updateList(
            filter: (com.barikoi.cnlapp.data.remote.models.pre_order.Product) -> Boolean,
            labelResId: Int,
            priceSelector: (com.barikoi.cnlapp.data.remote.models.pre_order.Product) -> String
        ) {
            productItems.clear()
            outlet.orders.forEach { order ->
                order.products.filter(filter).forEach { product ->
                    productItems.add(
                        ProductStatistics(
                            productId = product.productId,
                            productName = product.productName,
                            productCode = product.productCode,
                            skuCode = product.skuCode,
                            categoryCode = product.categoryCode,
                            categoryName = product.categoryName,
                            categoryId = product.categoryId,
                            unitName = product.unitName,
                            unitId = product.unitId,
                            unitCode = product.unitCode,
                            unitPrice = product.unitPrice.toDoubleOrNull() ?: 0.0,
                            discountedUnitPrice = product.discountedUnitPrice.toDoubleOrNull()
                                ?: 0.0,
                            orderedPrice = product.orderedAmount.toDoubleOrNull() ?: 0.0,
                            orderedQuantity = product.orderedQuantity.toIntOrNull() ?: 0,
                            availableQuantity = product.orderedQuantity.toIntOrNull() ?: 0,
                            bouncedQuantity = product.bouncedQuantity.toIntOrNull() ?: 0,
                            totalPrice = priceSelector(product).toDoubleOrNull() ?: 0.0
                        )
                    )
                }
            }

            tvItemCount.text = getString(R.string.items_, productItems.size.toString())
            val grandTotal = productItems.sumOf { it.totalPrice }
            tvGrandTotal.text = grandTotal.toString().format()

            val adapter = OutletProductAdapter()
            rcvProductList.layoutManager = LinearLayoutManager(requireContext())
            rcvProductList.adapter = adapter
            adapter.updateProducts(productItems)

            filterTitle.text = getString(labelResId)
        }

        if (orderStatus == "DELIVERED") {
            filterLayout.visibility = View.VISIBLE
            filterLayout.setOnClickListener {
                val popup = PopupMenu(requireContext(), filterTitle)
                popup.menuInflater.inflate(R.menu.filter_menu_orderstatus, popup.menu)
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_delivered_product -> updateList(
                            { (it.deliveredQuantity.toIntOrNull() ?: 0) > 0 },
                            R.string.delivered,
                            { it.deliveredAmount }
                        )

                        R.id.menu_bounced_product -> updateList(
                            { (it.bouncedQuantity.toIntOrNull() ?: 0) > 0 },
                            R.string.bounced,
                            { it.bouncedAmount }
                        )
                    }
                    true
                }
                popup.show()
            }

            updateList(
                { (it.deliveredQuantity.toIntOrNull() ?: 0) > 0 },
                R.string.delivered,
                { it.deliveredAmount }
            )
        } else {
            filterLayout.visibility = View.GONE
            val filter = when (orderStatus) {
                "PENDING" -> { p: com.barikoi.cnlapp.data.remote.models.pre_order.Product ->
                    (p.orderedQuantity.toIntOrNull() ?: 0) > 0
                }

                "CANCELLED" -> { p: com.barikoi.cnlapp.data.remote.models.pre_order.Product ->
                    (p.bouncedQuantity.toIntOrNull() ?: 0) > 0
                }

                else -> { _: com.barikoi.cnlapp.data.remote.models.pre_order.Product -> false }
            }
            val label = when (orderStatus) {
                "PENDING" -> R.string.pending
                "CANCELLED" -> R.string.bounced
                else -> R.string.unknown
            }
            val price = when (orderStatus) {
                "PENDING" -> { p: com.barikoi.cnlapp.data.remote.models.pre_order.Product -> p.orderedAmount }
                else -> { p: com.barikoi.cnlapp.data.remote.models.pre_order.Product -> p.bouncedAmount }
            }

            updateList(filter, label, price)
        }

        tvOrderStatus.text = getString(
            when (orderStatus) {
                "PENDING" -> R.string.pending
                "DELIVERED" -> R.string.delivered
                "CANCELLED" -> R.string.bounced
                else -> R.string.unknown
            }
        )


        when (orderStatus) {
            "PENDING" -> {
                val strokeColor = ContextCompat.getColor(requireContext(), R.color.status_pending_stroke)
                val fillColor = ContextCompat.getColor(requireContext(), R.color.status_pending)
                val borderColor = ContextCompat.getColor(requireContext(), R.color.white)

                statusLayout.background.setTint(strokeColor)

                val gradientDrawable = GradientDrawable().apply {
                    setColor(fillColor)
                    cornerRadius = 5f
                    setStroke(2, borderColor)
                }

                tvOrderStatus.background = gradientDrawable
            }

            "DELIVERED" -> {
                val strokeColor = ContextCompat.getColor(requireContext(), R.color.status_delivered_stroke)
                val fillColor = ContextCompat.getColor(requireContext(), R.color.status_delivered)
                val borderColor = ContextCompat.getColor(requireContext(), R.color.white)

                statusLayout.background.setTint(strokeColor)

                val backgroundDrawable = GradientDrawable().apply {
                    setColor(fillColor)
                    cornerRadius = 5f
                    setStroke(2, borderColor)
                }

                tvOrderStatus.background = backgroundDrawable
            }

            "CANCELLED" -> {
                val strokeColor = ContextCompat.getColor(requireContext(), R.color.status_bounced_stroke)
                val fillColor = ContextCompat.getColor(requireContext(), R.color.status_bounced)
                val borderColor = ContextCompat.getColor(requireContext(), R.color.white)

                statusLayout.background.setTint(strokeColor)

                val backgroundDrawable = GradientDrawable().apply {
                    setColor(fillColor)
                    cornerRadius = 5f
                    setStroke(2, borderColor)
                }

                tvOrderStatus.background = backgroundDrawable
            }

            else -> statusLayout.isVisible = false
        }

        btnClose.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
            dialog.dismiss()
        }

        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
    }
}