package com.barikoi.cnlapp.ProductStock

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barikoi.cnlapp.ProductStock.vm.ProductStockUpdateViewModel
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.ac.BaseActivity
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.data.remote.models.DbHouse
import com.barikoi.cnlapp.data.remote.models.Product
import com.barikoi.cnlapp.data.remote.models.Stock
import com.barikoi.cnlapp.data.remote.models.StockRequestModel
import com.barikoi.cnlapp.databinding.ActivityProductStockUpdateBinding
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.Constants
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.extension.englishToBanglaNumber
import com.barikoi.cnlapp.utils.extension.hideKeyboard
import com.barikoi.cnlapp.utils.extension.setHapticClickListener
import com.barikoi.cnlapp.utils.extension.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class ProductStockUpdateActivity : BaseActivity() {
    private lateinit var binding: ActivityProductStockUpdateBinding
    private val viewModel: ProductStockUpdateViewModel by viewModels()

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    private lateinit var adapter: ProductStockAdapter

    var selectedDBHouseId: String? = null

    var dbHouses: List<DbHouse> = emptyList()

    var productList: List<Product> = emptyList()
    private var productSelectMode = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppLogger.log("INIT:: ${this.javaClass.simpleName}")

        binding = ActivityProductStockUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        starDHObserve()
        starProductStockObserve()
        starStockRequestObserve()

        binding.toolbar.tvTitle.text = getString(R.string.title_product_stock_update)
        binding.toolbar.btnBack.setOnClickListener {
            if (productSelectMode) {
                cancelProductSelectionMode()
            } else {
                finish()
            }
        }

        onBackPressedDispatcher.addCallback(this) {
            if (productSelectMode) {
                cancelProductSelectionMode()
            } else {
                finish()
            }
        }

        binding.toolbar.btnClose.setOnClickListener {
            if (productSelectMode) {
                cancelProductSelectionMode()
            }
        }

        binding.refresh.setOnRefreshListener {
            if (productSelectMode) {
                binding.refresh.isRefreshing = false
                return@setOnRefreshListener
            }
            viewModel.getProductStock(
                "${getDate()} 00:00:00",
                "${getDate()} 23:59:59",
                "1",
                "1",
                sharePrefUtils.getString(Constants.DB_HOUSE_ID),
                sharePrefUtils.getString(Api.USER_ID)
            )
        }

        binding.btnSendStockRequest.setHapticClickListener {
            val products = getSelectedProduct()
            if (products.isNullOrEmpty()) {
                toast(getString(R.string.please_select_product))
            } else {
                stockRequestDialog(products)
            }
        }

        adapter = ProductStockAdapter(
            false,
            sharePrefUtils.getString(Api.USER_TYPE) == "TO",
            {
                if (sharePrefUtils.getString(Api.USER_TYPE) != "TO")
                    enableSelectionMode(it)

            }, {
                if (productSelectMode) {
                    selectProduct(it)
                }
            }, {
                if (productSelectMode) {
                    selectProduct(it)
                } else {
                    enableSelectionMode(it)
                }
            }, { text, position ->
                if (productSelectMode) {
                    productList[position].stockValue = text
                }

                AppLogger.log("stockValue:: ${productList[position].stockValue}")
            })

        binding.rcvProductList.layoutManager = LinearLayoutManager(this)
        binding.rcvProductList.adapter = adapter


        binding.rcvProductList.setOnTouchListener { _, _ ->
            hideKeyboard()
            false
        }

        binding.spinnerDistributorHouse.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    if (binding.spinnerDistributorHouse.adapter.count > 0) {
                        selectedDBHouseId = dbHouses[p2].id.toString()
                        viewModel.getProductStock(
                            "${getDate()} 00:00:00",
                            "${getDate()} 23:59:59",
                            "1",
                            "1",
                            selectedDBHouseId,
                            null
                        )
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }

        if (sharePrefUtils.getString(Api.USER_TYPE).equals("TO", true) ||
            sharePrefUtils.getString(Api.USER_TYPE).equals("ASM")
        ) {
            binding.spinnerLayoutRoute.visibility = View.VISIBLE
            viewModel.getDHList(sharePrefUtils.getString(Api.TERRITORY_ID)!!)
        } else {
            binding.spinnerLayoutRoute.visibility = View.GONE

            viewModel.getProductStock(
                "${getDate()} 00:00:00",
                "${getDate()} 23:59:59",
                "1",
                "1",
                sharePrefUtils.getString(Constants.DB_HOUSE_ID),
                sharePrefUtils.getString(Api.USER_ID)
            )
        }

    }

    private fun selectProduct(position: Int) {
        productList = productList.mapIndexed { index, product ->
            if (index == position) {
                product.copy(isSelect = !product.isSelect)
            } else {
                product
            }
        }

        adapter.updateProducts(productList)
        binding.toolbar.tvTitle.text = getString(
            R.string.selected,
            productList.filter { product -> product.isSelect }.size.toString()
                .englishToBanglaNumber()
        )
    }

    private fun enableSelectionMode(position: Int) {
        productSelectMode = true
        binding.toolbar.btnClose.isVisible = true
        binding.btnSendStockRequest.isVisible = true

        productList = productList.mapIndexed { index, product ->
            if (index == position) {
                product.copy(isSelect = true, stockValue = "0")
            } else {
                product.copy(isSelect = false, stockValue = "0")
            }
        }

        adapter.updateProducts(productList)

        binding.toolbar.tvTitle.text = getString(
            R.string.selected,
            productList.filter { product -> product.isSelect }.size.toString()
                .englishToBanglaNumber()
        )
    }

    private fun cancelProductSelectionMode() {
        val products = productList.map { product ->
            product.copy(isSelect = false)
        }
        adapter.updateProducts(products)
        productSelectMode = false
        binding.btnSendStockRequest.isVisible = false
        binding.toolbar.btnClose.isVisible = false
        binding.toolbar.btnBack.setImageResource(R.drawable.ic_back)
        binding.toolbar.tvTitle.text = getString(R.string.title_product_stock_update)
    }

    private fun getDate(): String {
        val c = Calendar.getInstance()
        c.set(Calendar.DAY_OF_MONTH, 1)
        val end = Calendar.getInstance().time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

        return df.format(end)
    }

    private fun stockRequestDialog(products: List<Stock>) {
        val dialog = Dialog(this@ProductStockUpdateActivity)
        dialog.setContentView(R.layout.dialog_stock_request)
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)
        dialog.window!!.attributes.windowAnimations = android.R.style.Animation_Dialog

        dialog.findViewById<View>(R.id.btnNo).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<View>(R.id.btnConfirm).setOnClickListener {
            dialog.dismiss()
            viewModel.sendRequest(
                StockRequestModel(
                    sharePrefUtils.getString(Api.EMPLOYEE_ID)!!,
                    sharePrefUtils.getString(Api.USER_ID)!!,
                    sharePrefUtils.getString(Constants.DB_HOUSE_ID)!!,
                    sharePrefUtils.getInt(Api.ROUTE_PAGE_SELECTED)!!.toString(),
                    products,
                    Constants.STOCK
                )
            )
        }

        dialog.show()
    }

    private fun getSelectedProduct(): List<Stock>? {
        val selectedProducts = productList.filter { product -> product.isSelect }
            .map { p ->
                if (p.stockValue == "0") {
                    toast("Please enter stock value")
                    return null
                } else {
                    Stock(p.stockValue.toInt(), p.id)
                }
            }

        if (selectedProducts.isEmpty()) {
            toast(getString(R.string.please_select_product))
            return null
        }

        return selectedProducts
    }

    private fun starStockRequestObserve() {
        lifecycleScope.launch {
            viewModel.productStockRequestResponse.observe(this@ProductStockUpdateActivity) {
                when (it) {
                    is ApiState.Empty -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("starStockRequestObserve::Empty")
                    }

                    is ApiState.Error -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("starStockRequestObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        binding.progressBar.isVisible = true
                        AppLogger.log("starStockRequestObserve::Loading")
                    }

                    is ApiState.Success -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("starStockRequestObserve:: Success ${it.data}")

                        toast(it.data!!.message)
                        if (productSelectMode) {
                            cancelProductSelectionMode()
                        }
                    }
                }
            }
        }
    }

    private fun starDHObserve() {
        lifecycleScope.launch {
            viewModel.dbHousesResponse.observe(this@ProductStockUpdateActivity) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("starDHObserve::Empty")
                    }

                    is ApiState.Error -> {
                        AppLogger.log("starDHObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("starDHObserve::Loading")
                    }

                    is ApiState.Success -> {
                        AppLogger.log("starDHObserve:: Success ${it.data}")

                        if (it.data?.dbHouses == null) {
                            toast("DB House List is empty")
                            return@observe
                        }
                        dbHouses = it.data.dbHouses

                        val dhNameList = it.data.dbHouses.map { dh -> dh.dbHouseName }
                        val adapter = ArrayAdapter(
                            applicationContext, android.R.layout.simple_spinner_item, dhNameList
                        )
                        binding.spinnerDistributorHouse.adapter = adapter
                    }
                }
            }
        }
    }

    private fun starProductStockObserve() {
        lifecycleScope.launch {
            viewModel.productResponse.observe(this@ProductStockUpdateActivity) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("starProductStockObserve::Empty")

                        binding.progressBar.isVisible = false
                        binding.refresh.isRefreshing = false
                        binding.tvEmptyTitle.isVisible = false
                    }

                    is ApiState.Error -> {
                        AppLogger.log("starProductStockObserve::Error ${it.error}")

                        binding.progressBar.isVisible = false
                        binding.refresh.isRefreshing = false
                        binding.tvEmptyTitle.isVisible = false
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("starProductStockObserve::Loading")

                        binding.progressBar.isVisible = true

                        binding.tvEmptyTitle.isVisible = false
                    }

                    is ApiState.Success -> {
                        AppLogger.log("starProductStockObserve:: Success ${it.data}")

                        binding.progressBar.isVisible = false
                        binding.refresh.isRefreshing = false

                        productList = it.data?.products ?: emptyList()
                        if (productList.isEmpty()) {
                            binding.tvEmptyTitle.isVisible = true
                            return@observe
                        }

                        adapter.updateProducts(it.data?.products!!)
                    }
                }
            }
        }
    }
}