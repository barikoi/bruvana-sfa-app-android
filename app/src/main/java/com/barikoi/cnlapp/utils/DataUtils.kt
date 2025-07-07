//package com.barikoi.cnlapp.utils
//
//import com.barikoi.cnlapp.data.remote.models.offer.Offer
//import com.barikoi.cnlapp.data.remote.models.offer.ProductCombination
//import com.barikoi.cnlapp.data.remote.models.product.Product
//import com.barikoi.cnlapp.data.remote.models.request.order.toProductRequestList1
//
//class DataUtils {
//
//
//    val productList = listOf(
//        Product(
//            brandId = 1,
//            categoryCode = "GRC",
//            categoryId = 10,
//            categoryName = "Groceries",
//            createdAt = "2024-01-01",
//            currentAvailableStock = 100,
//            currentStock = 120,
//            discountAmount = "5",
//            discountedUnitPrice = 45.0,
//            dpPrice = "48.0",
//            dpPriceCartoon = null,
//            etpPriceCartoon = null,
//            etpPricePackJar = null,
//            etpPriceUnit = "50.0",
//            id = 101,
//            images = emptyList(),
//            initialAvailableStock = 120,
//            1,
//            initialStock = 120,
//            isActive = 1,
//            modelNo = "",
//            proCode = "SUG101",
//            productCode = "SUG123",
//            productName = "Sugar",
//            skuCode = "SKU123",
//            tpPriceCartoon = "0",
//            unitCode = "KG",
//            unitId = 1,
//            unitName = "Kilogram",
//            unitPrice = "50.0",
//            updatedAt = "2024-06-01",
//            qty = 2
//        ),
//        Product(
//            brandId = 1,
//            categoryCode = "GRC",
//            categoryId = 11,
//            categoryName = "Groceries",
//            createdAt = "2024-01-01",
//            currentAvailableStock = 50,
//            currentStock = 60,
//            discountAmount = "5",
//            discountedUnitPrice = 65.0,
//            dpPrice = "68.0",
//            dpPriceCartoon = null,
//            etpPriceCartoon = null,
//            etpPricePackJar = null,
//            etpPriceUnit = "70.0",
//            id = 102,
//            images = emptyList(),
//            initialAvailableStock = 60,
//            1,
//            initialStock = 60,
//            isActive = 1,
//            modelNo = "",
//            proCode = "RIC102",
//            productCode = "RIC456",
//            productName = "Rice",
//            skuCode = "SKU456",
//            tpPriceCartoon = "0",
//            unitCode = "KG",
//            unitId = 2,
//            unitName = "Kilogram",
//            unitPrice = "70.0",
//            updatedAt = "2024-06-01",
//            qty = 1
//        )
//    )
//
//    val offers = listOf(
//        Offer(
//            comboPrice = "90.0",
//            createdAt = "2024-01-01T00:00:00Z",
//            description = "Sugar + Salt Combo",
//            endDate = "2024-12-31",
//            id = 1,
//            isActive = true,
//            name = "Combo 1",
//            originalPrice = "100.0",
//            productCombinations = listOf(
//                ProductCombination(
//                    productId = 101,
//                    product = com.barikoi.cnlapp.data.remote.models.offer.Product(
//                        discountedUnitPrice = "45.0",
//                        id = 101,
//                        name = "Sugar",
//                        unit = "Kilogram"
//                    ),
//                    quantity = 1
//                ),
//                ProductCombination(
//                    productId = 202,
//                    product = com.barikoi.cnlapp.data.remote.models.offer.Product(
//                        discountedUnitPrice = "20.0",
//                        id = 202,
//                        name = "Salt",
//                        unit = "Kilogram"
//                    ),
//                    quantity = 2
//                )
//            ),
//            startDate = "2024-01-01",
//            updatedAt = "2024-06-25",
//            quantity = 2 // Combo selected 2 times
//        ),
//
//        Offer(
//            comboPrice = "130.0",
//            createdAt = "2024-02-01T00:00:00Z",
//            description = "Oil + Rice Combo",
//            endDate = "2024-12-31",
//            id = 2,
//            isActive = true,
//            name = "Combo 2",
//            originalPrice = "150.0",
//            productCombinations = listOf(
//                ProductCombination(
//                    productId = 102,
//                    product = com.barikoi.cnlapp.data.remote.models.offer.Product(
//                        discountedUnitPrice = "65.0",
//                        id = 102,
//                        name = "Rice",
//                        unit = "Kilogram"
//                    ),
//                    quantity = 2
//                ),
//                ProductCombination(
//                    productId = 101,
//                    product = com.barikoi.cnlapp.data.remote.models.offer.Product(
//                        discountedUnitPrice = "60.0",
//                        id = 101,
//                        name = "Oil",
//                        unit = "Liter"
//                    ),
//                    quantity = 2
//                )
//            ),
//            startDate = "2024-02-01",
//            updatedAt = "2024-06-25",
//            quantity = 1
//        )
//    )
//
//    val productRequests = productList.toProductRequestList1(offers)
//    productRequests.forEach {
//        println("Product: ${it.productName}, price: ${it.discountedUnitPrice} Qty: ${it.orderedQuantity}, Amount: ${it.orderedAmount}")
//    }
//
//
//
//}