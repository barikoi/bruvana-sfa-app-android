package com.barikoi.cnlapp.order_create.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.ui.main.MainActivity

class SelectDokanFragment : Fragment() {
    lateinit var ACTIVITY: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ACTIVITY = context as MainActivity

        val bundle = this.arguments
        if (bundle != null) {
            if (bundle.containsKey("from")) {
                if (bundle.getString("from").equals("Order")) {
                    bundle.getSerializable("Order")?.let {
                        CreateOrderFragment.startFragmentWithValue(
                            "Order",
                            it,
                            ProductSelectFragment(),
                            ACTIVITY
                        )
                    }
                } else {
                    CreateOrderFragment.setCurrentFragment(ShopSelectFragment(), ACTIVITY)
                }
            }
        } else {
            CreateOrderFragment.setCurrentFragment(ShopSelectFragment(), ACTIVITY)
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_select_dokan, container, false)
    }

    override fun onResume() {
        super.onResume()
        Log.d("Order", "onResume select dokan")
    }
}