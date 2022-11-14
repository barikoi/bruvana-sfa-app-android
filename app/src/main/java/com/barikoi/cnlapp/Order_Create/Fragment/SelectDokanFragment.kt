package com.barikoi.cnlapp.Order_Create.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.barikoi.cnlapp.Activity.MainActivity
import com.barikoi.cnlapp.R

class SelectDokanFragment : Fragment() {
    lateinit var ACTIVITY: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ACTIVITY = context as MainActivity
        CreateOrderFragment.setCurrentFragment(ShopSelectFragment(), ACTIVITY)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_select_dokan, container, false)
    }

}