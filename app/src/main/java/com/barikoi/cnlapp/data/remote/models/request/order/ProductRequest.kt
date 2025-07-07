package com.barikoi.cnlapp.data.remote.models.request.order


import com.barikoi.cnlapp.data.remote.models.offer.Offer
import com.barikoi.cnlapp.data.remote.models.product.Product
import com.barikoi.cnlapp.utils.AppLogger
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class ProductRequest(
    @SerializedName("bounced_amount")
    val bouncedAmount: String,
    @SerializedName("bounced_quantity")
    val bouncedQuantity: String,
    @SerializedName("category_code")
    val categoryCode: String,
    @SerializedName("category_id")
    val categoryId: String,
    @SerializedName("category_name")
    val categoryName: String,
    @SerializedName("delivered_amount")
    val deliveredAmount: String,
    @SerializedName("delivered_quantity")
    val deliveredQuantity: String,
    @SerializedName("discounted_unit_price")
    val discountedUnitPrice: String,
    @SerializedName("ordered_amount")
    val orderedAmount: String,
    @SerializedName("ordered_quantity")
    val orderedQuantity: String,
    @SerializedName("product_code")
    val productCode: String,
    @SerializedName("product_id")
    val productId: String,
    @SerializedName("product_name")
    val productName: String,
    @SerializedName("sku_code")
    val skuCode: String,
    @SerializedName("unit_code")
    val unitCode: String,
    @SerializedName("unit_id")
    val unitId: String,
    @SerializedName("unit_name")
    val unitName: String,
    @SerializedName("unit_price")
    val unitPrice: String
)

fun Product.toProductRequest(): ProductRequest {
    val quantityStr = qty.toString()
    val unitPriceStr = unitPrice

    return ProductRequest(
        bouncedAmount = "0", // or calculate if applicable
        bouncedQuantity = "0", // or calculate if applicable
        categoryCode = categoryCode,
        categoryId = categoryId.toString(),
        categoryName = categoryName,
        deliveredAmount = "0",
        deliveredQuantity = "0",
        discountedUnitPrice = discountedUnitPrice.toString(),
        orderedAmount = "0",
        orderedQuantity = quantityStr,
        productCode = productCode,
        productId = id.toString(),
        productName = productName,
        skuCode = skuCode,
        unitCode = unitCode,
        unitId = unitId.toString(),
        unitName = unitName,
        unitPrice = unitPriceStr
    )
}

fun List<Product>.toProductRequestList(
    offers: List<Offer> = emptyList()
): List<ProductRequest> {
    val productsWithQuantity: List<Offer> = offers.filter { it.quantity > 0 }

    return this.filter { it.qty > 0 }
        .map { product ->
            val orderedQuantity = product.qty
            val orderedAmount = (product.discountedUnitPrice) * orderedQuantity

            ProductRequest(
                bouncedAmount = "0",
                bouncedQuantity = "0",
                categoryCode = product.categoryCode,
                categoryId = product.categoryId.toString(),
                categoryName = product.categoryName,
                deliveredAmount = "0",
                deliveredQuantity = "0",
                discountedUnitPrice = product.discountedUnitPrice.toString(),
                orderedAmount = orderedAmount.toString(),
                orderedQuantity = orderedQuantity.toString(),
                productCode = product.productCode,
                productId = product.id.toString(),
                productName = product.productName,
                skuCode = product.skuCode,
                unitCode = product.unitCode,
                unitId = product.unitId.toString(),
                unitName = product.unitName,
                unitPrice = product.unitPrice
            )
        }
}

fun List<Product>.toProductRequestList1(
    offers: List<Offer> = emptyList()
): List<ProductRequest> {
    val updatedProductList = this.toMutableList()

    offers.filter { it.quantity > 0 }.forEach { offer ->
        offer.productCombinations.forEach { combo ->
            val existing = updatedProductList.find { it.id == combo.product.id }
            if (existing != null) {
                // Product exists → increase quantity
                val updated = existing.copy(qty = existing.qty + combo.quantity * offer.quantity)
                val index = updatedProductList.indexOf(existing)
                updatedProductList[index] = updated
            } else {
                AppLogger.log("Product with ID ${combo.productId} not found in the product list.")
            }
        }
    }

    val ss = updatedProductList
        .filter { it.qty > 0 }
        .map { product ->
            val orderedAmount = product.discountedUnitPrice * product.qty
            ProductRequest(
                bouncedAmount = "0",
                bouncedQuantity = "0",
                categoryCode = product.categoryCode,
                categoryId = product.categoryId.toString(),
                categoryName = product.categoryName,
                deliveredAmount = "0",
                deliveredQuantity = "0",
                discountedUnitPrice = product.discountedUnitPrice.toString(),
                orderedAmount = orderedAmount.toString(),
                orderedQuantity = product.qty.toString(),
                productCode = product.productCode,
                productId = product.id.toString(),
                productName = product.productName,
                skuCode = product.skuCode,
                unitCode = product.unitCode,
                unitId = product.unitId.toString(),
                unitName = product.unitName,
                unitPrice = product.unitPrice
            )
        }

    AppLogger.log("Update Size ${ss.size} ProductRequest:: ${Gson().toJson(ss)}")
    return ss
}