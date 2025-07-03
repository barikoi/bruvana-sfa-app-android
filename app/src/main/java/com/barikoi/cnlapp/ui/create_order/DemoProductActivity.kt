package com.barikoi.cnlapp.ui.create_order

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.data.remote.models.offer.Offer
import com.barikoi.cnlapp.data.remote.models.offer.Product
import com.barikoi.cnlapp.data.remote.models.offer.ProductCombination
import com.barikoi.cnlapp.ui.create_order.order.combo.AdapterOffer

class DemoProductActivity : AppCompatActivity() {
    private lateinit var adapterOffer: AdapterOffer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo_product)

        adapterOffer = AdapterOffer(
            onPlusClick = { offer, position ->

            },
            onMinusClick = { offer, position ->
            }
        )

        val rcv = findViewById<RecyclerView>(R.id.rcvOffer)
        rcv.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        rcv.adapter = adapterOffer

        val demoOffers = mutableListOf<Offer>()

        for (i in 1..15) {
            val productCombinations = listOf(
                ProductCombination(
                    product = Product(
                        id = i * 10,
                        name = "Product A $i",
                        unit = "pcs",
                        discountedUnitPrice = "50"
                    ),
                    productId = i * 10,
                    quantity = 2
                ),
                ProductCombination(
                    product = Product(
                        id = i * 10 + 1,
                        name = "Product B $i",
                        unit = "kg",
                        discountedUnitPrice = "100"
                    ),
                    productId = i * 10 + 1,
                    quantity = 1
                )
            )

            val offer = Offer(
                id = i,
                name = "Combo Offer $i",
                description = "Get a combo discount on products",
                originalPrice = "200",
                comboPrice = "150",
                startDate = "2025-07-01",
                endDate = "2025-07-31",
                createdAt = "2025-07-01T00:00:00Z",
                updatedAt = null,
                isActive = true,
                productCombinations = productCombinations,
                quantity = 0
            )

            demoOffers.add(offer)
        }

        adapterOffer.updateData(demoOffers)
    }
}