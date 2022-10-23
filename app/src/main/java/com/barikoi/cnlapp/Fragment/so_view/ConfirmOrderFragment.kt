package com.barikoi.cnlapp.Fragment.so_view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.barikoi.cnlapp.Adapter.so_view.ConfirmOrderList
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.RoomDb.AppDatabase
import com.barikoi.cnlapp.RoomDb.OrderList
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.RequestQueueSingleton


class ConfirmOrderFragment : Fragment() {

    var recylerView: RecyclerView? = null
    var latitude: Double? = 0.0
    var longitude: Double? = 0.0
    var user_id : String? = null
    var confirmOrder: AppCompatButton? = null
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var queue: RequestQueue? = null
    var appDatabase: AppDatabase? = null
    var allorderList: List<OrderList> ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_confirm_order, container, false)

        confirmOrder = view.findViewById(R.id.btnConfirm)
        recylerView = view.findViewById(R.id.orderList)

        getAllOrdersDB()
        return view
    }

    private fun getAllOrdersDB() {
        allorderList = appDatabase!!.orderListDao().getOrdersDB(prefs!!.getString(Api.SELECTED_SHOP_ID, "")!!)

        if (allorderList!!.size > 0){
            val adapter = ConfirmOrderList(allorderList!!)
            recylerView!!.adapter = adapter
            adapter.notifyDataSetChanged()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        queue = RequestQueueSingleton.getInstance(context).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        editor = prefs!!.edit()
        user_id = prefs!!.getString(Api.USER_ID, "")
        appDatabase = AppDatabase.getInstance(context)
        mContext = context

    }

}