package com.barikoi.cnlapp.Order_Create.Fragment

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
import com.barikoi.cnlapp.callback.OnBackPressedListener
import java.io.Serializable

class CreateOrderFragment : Fragment(), OnBackPressedListener{
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

    /*fun setCurrentFragment(fragment: Fragment?, activity: Activity) {
        val fragmentManager = (activity as FragmentActivity).supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentLayout, fragment!!)
        fragmentTransaction.commit()
        //fragmentManager.executePendingTransactions()
    }*/

    companion object{
        fun startFragmentWithValue(
            key: String,
            value: Serializable,
            fragmentName: Fragment,
            activity: Activity
        ) {
            val bundle = Bundle()
            bundle.putString("from", key)
            bundle.putSerializable(key, value) // Put anything what you want

            fragmentName.arguments = bundle
            val fragmentManager = (activity as FragmentActivity).supportFragmentManager
            fragmentManager.beginTransaction()
                .replace(R.id.fragmentLayout, fragmentName)
                .commit()
        }

        fun setCurrentFragment(fragment: Fragment?, activity: Activity) {
            val fragmentManager = (activity as FragmentActivity).supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragmentLayout, fragment!!)
            fragmentTransaction.commit()
            //fragmentManager.executePendingTransactions()
        }
    }

    override fun onBackPressed() {
        /*val fragment =
            this.supportFragmentManager.findFragmentById(R.id.main_container)
        (fragment as? IOnBackPressed)?.onBackPressed()?.not()?.let {

        }*/
    }


}