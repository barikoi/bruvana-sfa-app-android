package com.barikoi.cnlapp.Fragment.so_view

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.barikoi.cnlapp.Activity.MainActivity
import com.barikoi.cnlapp.R

class CreateOrderFragment : Fragment() {
    lateinit var ACTIVITY: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ACTIVITY = context as MainActivity
        setCurrentFragment(ShopSelectFragment(), ACTIVITY)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_order, container, false)
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    fun setCurrentFragment(fragment: Fragment?, activity: Activity) {
        val fragmentManager = (activity as FragmentActivity).supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentLayout, fragment!!)
        fragmentTransaction.commit()
        //fragmentManager.executePendingTransactions()
    }
}