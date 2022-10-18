package com.barikoi.cnlapp.Fragment.so_view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.barikoi.cnlapp.Activity.MainActivity
import com.barikoi.cnlapp.Adapter.so_view.ShopSelectAdapter
import com.barikoi.cnlapp.Model.Products
import com.barikoi.cnlapp.Model.Shops
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.MoreSpinner
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.callback.OnSelectListener

class ProductSelectFragment : Fragment() {

    var recylerView: RecyclerView? = null
    var mContext: Context? = null
    var queue: RequestQueue? = null
    var user_id : String? = null
    var listener: OnSelectListener? = null
    var et_search: AutoCompleteTextView? = null
    private var adapter: ShopSelectAdapter? = null
    var productsList: ArrayList<Products>? = ArrayList()
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var loading: ProgressBar? = null
    lateinit var ACTIVITY: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_product_select, container, false)
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        queue = RequestQueueSingleton.getInstance(context).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        editor = prefs!!.edit()
        //token = prefs.getString("token", "")
        user_id = prefs!!.getString(Api.USER_ID, "")
        mContext = context
    }

}