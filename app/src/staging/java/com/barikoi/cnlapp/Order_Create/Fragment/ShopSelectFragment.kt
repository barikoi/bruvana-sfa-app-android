package com.barikoi.cnlapp.Order_Create.Fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.barikoi.cnlapp.Activity.MainActivity
import com.barikoi.cnlapp.Model.Shops
import com.barikoi.cnlapp.Order_Create.Adapter.ShopSelectAdapter
import com.barikoi.cnlapp.Order_Create.Callback.OnSelectListener
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.RoomDb.AppDatabase
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.MoreSpinner
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.google.android.gms.location.*
import io.sentry.Sentry
import kotlinx.android.synthetic.main.fragment_shop_select.*
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException


class ShopSelectFragment : Fragment(), OnSelectListener {
    var recylerView: RecyclerView? = null
    var mContext: Context? = null
    var queue: RequestQueue? = null
    var spinner : MoreSpinner? = null
    var user_id : String? = null
    var sr_id : String? = null
    var routeId: String ? = null
    var token: String ? = null
    var listener: OnSelectListener? = null
    var et_search: AutoCompleteTextView? = null
    private var adapter: ShopSelectAdapter? = null
    var shopList: ArrayList<Shops>? = ArrayList()
    var filterList: ArrayList<Shops>? = ArrayList()
    var routeNameList: ArrayList<Pair<String, String>>? = ArrayList()
    val routesList = ArrayList<String>()
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var loading: ProgressBar? = null
    private var appDatabase: AppDatabase? = null
    lateinit var ACTIVITY: MainActivity
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationCallback: LocationCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        sortingLayout.setOnClickListener {
            val popup = PopupMenu(mContext, sortTitle)
            popup.menuInflater.inflate(R.menu.sort_menu_outlet, popup.menu)
            popup.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener,
                PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    when(item.itemId){
                        R.id.menu_ztoa->{
                            shopList!!.sortByDescending {
                                it.shop_name
                            }
                            if (shopList!!.size > 0){
                                adapter = ShopSelectAdapter(shopList!!, listener!!)
                                recylerView!!.adapter = adapter
                                adapter!!.notifyDataSetChanged()
                            }

                            sortTitle!!.setText(resources.getString(R.string.ztoa))
                        }
                        R.id.menu_atoz->{
                            shopList!!.sortBy {
                                it.shop_name
                            }
                            if (shopList!!.size > 0){
                                adapter = ShopSelectAdapter(shopList!!, listener!!)
                                recylerView!!.adapter = adapter
                                adapter!!.notifyDataSetChanged()
                            }
                            sortTitle!!.setText(resources.getString(R.string.atoz))
                        }
                    }
                    return true
                }


            })
            popup.show()
        }

        filterLayout.setOnClickListener {
            val popup = PopupMenu(mContext, filterTitle)
            popup.menuInflater.inflate(R.menu.filter_menu_outlets, popup.menu)
            popup.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener,
                PopupMenu.OnMenuItemClickListener {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    when(item.itemId){
                        R.id.menu_All->{
                            adapter = ShopSelectAdapter(shopList!!, listener!!)
                            shoplist.adapter = adapter
                            adapter!!.notifyDataSetChanged()
                        }
                        R.id.menu_A->{
                            filterList!!.clear()
                            filterList!!.addAll(shopList!!)
                            try {
                                filterList!!.removeIf {
                                    !it.category.get(0).toString().equals("A", true)
                                }
                            }catch (e: Exception){
                                e.printStackTrace()
                            }
                            if (filterList!!.size > 0){
                                adapter = ShopSelectAdapter(filterList!!, listener!!)
                                shoplist.adapter = adapter
                                adapter!!.notifyDataSetChanged()
                            }
                        }
                        R.id.menu_B->{
                            filterList!!.clear()
                            filterList!!.addAll(shopList!!)

                            try {
                                filterList!!.removeIf {
                                    !it.category.get(0).toString().equals("B", true)
                                }
                            }catch (e: Exception){
                                e.printStackTrace()
                            }
                            adapter = ShopSelectAdapter(filterList!!, listener!!)
                            shoplist.adapter = adapter
                            adapter!!.notifyDataSetChanged()
                        }
                        R.id.menu_C->{
                            filterList!!.clear()
                            filterList!!.addAll(shopList!!)
                            try {
                                filterList!!.removeIf {
                                    !it.category.get(0).toString().equals("C", true)
                                }
                            }catch (e: Exception){
                                e.printStackTrace()
                            }
                            adapter = ShopSelectAdapter(filterList!!, listener!!)
                            shoplist.adapter = adapter
                            adapter!!.notifyDataSetChanged()
                        }
                        R.id.menu_D->{
                            filterList!!.clear()
                            filterList!!.addAll(shopList!!)
                            try {
                                filterList!!.removeIf {
                                    !it.category.get(0).toString().equals("D", true)
                                }
                            }catch (e: Exception){
                                e.printStackTrace()
                            }
                            adapter = ShopSelectAdapter(filterList!!, listener!!)
                            shoplist.adapter = adapter
                            adapter!!.notifyDataSetChanged()
                        }
                        R.id.menu_E->{
                            filterList!!.clear()
                            filterList!!.addAll(shopList!!)
                            try {
                                filterList!!.removeIf {
                                    !it.category.get(0).toString().equals("E", true)
                                }
                            }catch (e: Exception){
                                e.printStackTrace()
                            }
                            adapter = ShopSelectAdapter(filterList!!, listener!!)
                            shoplist.adapter = adapter
                            adapter!!.notifyDataSetChanged()
                        }
                        R.id.menu_F->{
                            filterList!!.clear()
                            filterList!!.addAll(shopList!!)
                            try {
                                filterList!!.removeIf {
                                    !it.category.get(0).toString().equals("F", true)
                                }
                            }catch (e: Exception){
                                e.printStackTrace()
                            }
                            adapter = ShopSelectAdapter(filterList!!, listener!!)
                            shoplist.adapter = adapter
                            adapter!!.notifyDataSetChanged()
                        }
                        R.id.menu_pharmacy->{
                            filterList!!.clear()
                            filterList!!.addAll(shopList!!)
                            try {
                                filterList!!.removeIf {
                                    !it.category.get(0).toString().equals("P", true)
                                }
                            }catch (e: Exception){
                                e.printStackTrace()
                            }
                            adapter = ShopSelectAdapter(filterList!!, listener!!)
                            shoplist.adapter = adapter
                            adapter!!.notifyDataSetChanged()
                        }
                        R.id.menu_mpharma->{
                            filterList!!.clear()
                            filterList!!.addAll(shopList!!)
                            try {
                                filterList!!.removeIf {
                                    !it.category.get(0).toString().equals("M", true)
                                }
                            }catch (e: Exception){
                                e.printStackTrace()
                            }
                            adapter = ShopSelectAdapter(filterList!!, listener!!)
                            shoplist.adapter = adapter
                            adapter!!.notifyDataSetChanged()
                        }
                        R.id.menu_warehouse->{
                            filterList!!.clear()
                            filterList!!.addAll(shopList!!)
                            try {
                                filterList!!.removeIf {
                                    !it.category.get(0).toString().equals("W", true)
                                }
                            }catch (e: Exception){
                                e.printStackTrace()
                            }
                            adapter = ShopSelectAdapter(filterList!!, listener!!)
                            shoplist.adapter = adapter
                            adapter!!.notifyDataSetChanged()
                        }
                        R.id.menu_no_order->{
                            filterList!!.clear()
                            filterList!!.addAll(shopList!!)
                            filterList!!.removeIf {
                                it.isNoOrdered != 1
                            }
                            adapter = ShopSelectAdapter(filterList!!, listener!!)
                            shoplist.adapter = adapter
                            adapter!!.notifyDataSetChanged()
                        }
                        R.id.menu_ordered->{
                            filterList!!.clear()
                            filterList!!.addAll(shopList!!)
                            filterList!!.removeIf {
                                it.isOrdered != 1
                            }
                            adapter = ShopSelectAdapter(filterList!!, listener!!)
                            shoplist.adapter = adapter
                            adapter!!.notifyDataSetChanged()
                        }
                    }
                    return true
                }


            })
            popup.show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_shop_select, container, false)
        recylerView = view.findViewById(R.id.shoplist)
        loading = view.findViewById(R.id.progressBar)
        spinner = view.findViewById(R.id.spinnerRoutes)
        et_search = view.findViewById(R.id.editTextSearchShop)
        adapter = ShopSelectAdapter( ArrayList<Shops>(), listener!!)
        recylerView!!.adapter = adapter

        var selectedRoute = prefs!!.getString(Api.SELECTED_ROUTE_NAME, "")
        if (routeNameList!!.size == 0) {
            getAllRoutes(Api.routes_withfilter + "?with_geometry=0&user_id=" + user_id)
        }else{
            if (spinner != null) {
                if (spinner!!.adapter == null){
                    val adapter = ArrayAdapter(
                        mContext!!,
                        android.R.layout.simple_spinner_item, routesList
                    )
                    spinner!!.adapter = adapter
                }

            }
        }
        spinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                /*for (i in 0 until routeNameList!!.size) {
                    Log.d("RouteList", "all 2 "+ routeNameList!![i].second)
                }*/
                if(selectedRoute!!.length>0){
                    val pos= (spinner!!.adapter as ArrayAdapter<String>).getPosition(selectedRoute)
                    if(pos>-1) {
                        selectedRoute = ""
                        spinner!!.setSelection(pos)
                        return
                    }
                }
                Log.d("RouteList", "position: "+p2)
                Log.d("RouteList", "size: "+routeNameList!!.size)
                val route_id = routeNameList!![p2].first
                /*editor!!.putString(Api.SELECTED_ROUTE_ID, route_id)
                editor!!.commit()*/
                getShopListbyRoute(Api.verified_shop_list+"?route_id="+route_id+"&user_id="+user_id)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        et_search!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter!!.filter.filter(s)
                if (s!!.length == 0) {
                    val shops: ArrayList<Shops> = ArrayList()
                    if (shopList!!.size >0) {
                        for (i in 0 until shopList!!.size) {
                            if (shopList!![i].route_name == routeNameList!![spinner!!.selectedItemPosition].second) {
                                shops.add(shopList!![i])
                            }

                        }
                    }
                    //adapter!!.shopList=shops
                    adapter = ShopSelectAdapter(shops, listener!!)
                    recylerView!!.adapter = adapter
                    adapter!!.notifyDataSetChanged()
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        return view
    }

    private fun getAllRoutes(url: String) {
        //loading!!.visibility = View.VISIBLE
        routeNameList!!.clear()
        routesList.clear()
        val request = StringRequest(Request.Method.GET, url,
            {
                response ->
                try {
                    //loading!!.visibility = View.GONE
                    val data = JSONObject(response)
                    if (data.has("routes") && !data.isNull("routes")){

                        val routesArray = data.getJSONArray("routes")
                        if (routesArray.length() > 0){

                            for(i in 0 until routesArray.length()){
                                val routeObj = routesArray.getJSONObject(i)

                                routeNameList!!.add(Pair(routeObj.getString("id"), routeObj.getString("route_name")))
                                routesList.add(routeObj.getString("route_name"))
                            }
                            /*for (i in 0 until ShopListFragment.allRouteList!!.size) {
                                Log.d("RouteList", "all 2 "+ ShopListFragment.allRouteList!![i].route_name+" "+ ShopListFragment.allRouteList!![i].shopList.size.toString())
                            }*/
                            if (spinner != null) {
                                if (spinner!!.adapter == null){
                                    val adapter = ArrayAdapter(
                                        mContext!!,
                                        android.R.layout.simple_spinner_item, routesList
                                    )
                                    spinner!!.adapter = adapter
                                }

                            }
                        }

                    }
                }catch (e:Exception){
                    Sentry.captureException(e)
                    e.printStackTrace()
                }
            },
            { error ->
                //loading!!.visibility = View.GONE
                if (error is TimeoutError) {
                    Toast.makeText(mContext, "Request timeout!! Check your internet connection or Contact Admin", Toast.LENGTH_LONG).show()
                }
                if (error is NoConnectionError) {
                    //mListerner.onFailure("Turn on your internet connection and Try again")
                    Toast.makeText(mContext, "Turn on your internet connection and Try again", Toast.LENGTH_LONG).show()
                }
                if (error != null && error.networkResponse != null) {
                    try {
                        val s = String(error.networkResponse.data)
                        Log.d("Routes", "message: $s")
                        val data = JSONObject(s)
                        //Toast.makeText(mContext.getApplicationContext(), data.getString("message"), Toast.LENGTH_SHORT).show();
                        //mListerner.onFailure(data.getString("message"))
                        Toast.makeText(mContext, data.getString("message"), Toast.LENGTH_LONG).show()
                    } catch (e: UnsupportedEncodingException) {
                        Sentry.captureException(e)
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        //mListerner.onFailure(e.message)
                        Sentry.captureException(e)
                        Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                    }
                }
            })
        queue!!.add(request)

    }

    private fun getShopListbyRoute(url: String) {
        loading!!.visibility = View.VISIBLE
        val request = StringRequest(Request.Method.GET, url,
            {
                    response ->
                try {
                    loading!!.visibility = View.GONE
                    val data = JSONObject(response)
                    if (data.has("outlets") && !data.isNull("outlets")){
                        val outletsArray = data.getJSONArray("outlets")
                        if (outletsArray.length() > 0){
                            shopList!!.clear()
                            if (outletsArray.length()>0){
                                for (i in 0 until outletsArray.length()){
                                    var imageUrl = "null"
                                    val outletObj = outletsArray.getJSONObject(i)
                                    if (outletObj.has("images") && !outletObj.isNull("images")){
                                        val imageArray = outletObj.getJSONArray("images")
                                        if (imageArray.length() > 0){
                                            val imageobj = imageArray.getJSONObject(0)
                                            if (imageobj.has("image_url")){
                                                imageUrl = imageobj.getString("image_url")
                                            }
                                        }
                                    }
                                    val shops = Shops(
                                        outletObj.getString("id"),
                                        outletObj.getString("outlet_name"),
                                        outletObj.getString("outlet_status"),
                                        outletObj.getString("address"),
                                        outletObj.getString("outlet_code"),
                                        outletObj.getString("outlet_type"),
                                        outletObj.getString("outlet_category"),
                                        outletObj.getString("owner_name"),
                                        outletObj.getString("minimum_order"),
                                        outletObj.getString("market_opportunity"),
                                        outletObj.getString("phone_number") ,
                                        outletObj.getInt("is_buyer"),
                                        /*outletObj.getString("distributor_office"),
                                        outletObj.getString("distributor_office_code"),*/
                                        imageUrl,ArrayList(),"",
                                        outletObj.getDouble("latitude"),
                                        outletObj.getDouble("longitude"),
                                        outletObj.getString("route_id"),
                                        outletObj.getString("route_name"),
                                        outletObj.getString("last_ordered_at"),
                                        outletObj.getInt("is_verified"),
                                        outletObj.getInt("ordered_today"),
                                        outletObj.getInt("is_no_order")
                                    )

                                    shopList!!.add(shops)
                                }

                                if (shopList!!.size > 0){
                                    adapter = ShopSelectAdapter(shopList!!, listener!!)
                                    recylerView!!.adapter = adapter
                                    adapter!!.notifyDataSetChanged()
                                }
                            }
                            if (spinner != null) {
                                if (spinner!!.adapter == null){
                                    val adapter = ArrayAdapter(
                                        mContext!!,
                                        android.R.layout.simple_spinner_item, routeNameList!!
                                    )
                                    spinner!!.adapter = adapter
                                }

                            }
                        }

                    }
                }catch (e:Exception){
                    Sentry.captureException(e)
                    e.printStackTrace()
                }
            },
            { error ->
                loading!!.visibility = View.GONE
                if (error is TimeoutError) {
                    //mListerner.onFailure("Request timeout!! Check your internet connection or Contact Admin")
                    Toast.makeText(mContext, "Request timeout!! Check your internet connection or Contact Admin", Toast.LENGTH_LONG).show()
                }
                if (error is NoConnectionError) {
                    //mListerner.onFailure("Turn on your internet connection and Try again")
                    Toast.makeText(mContext, "Turn on your internet connection and Try again", Toast.LENGTH_LONG).show()
                }
                if (error != null && error.networkResponse != null) {
                    try {
                        val s = String(error.networkResponse.data)
                        Log.d("Routes", "message: $s")
                        val data = JSONObject(s)
                        //Toast.makeText(mContext.getApplicationContext(), data.getString("message"), Toast.LENGTH_SHORT).show();
                        //mListerner.onFailure(data.getString("message"))
                        Toast.makeText(mContext, data.getString("message"), Toast.LENGTH_LONG).show()
                    } catch (e: UnsupportedEncodingException) {
                        Sentry.captureException(e)
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        //mListerner.onFailure(e.message)
                        Sentry.captureException(e)
                        Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                    }
                }
            })
        request.retryPolicy = DefaultRetryPolicy(
            60 * 1000, 0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue!!.add(request)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        queue = RequestQueueSingleton.getInstance(context).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        editor = prefs!!.edit()
        token = prefs!!.getString(Api.TOKEN, "")
        user_id = prefs!!.getString(Api.USER_ID, "")
        sr_id = prefs!!.getString(Api.SR_CODE, "")
        routeId = prefs!!.getString(Api.SELECTED_ROUTE_ID, "")
        mContext = context
        listener = this
        appDatabase = AppDatabase.getInstance(context)
        ACTIVITY = context as MainActivity

        /*if (routeNameList!!.size == 0) {
            getAllRoutes(Api.routes_withfilter + "?with_geometry=0&sr_id=" + user_id)
        }*/
    }

    override fun onShopSelected(shop: Shops) {
        editor!!.putString(Api.SELECTED_SHOP_ID, shop.shop_id)
        editor!!.commit()
        appDatabase!!.saveOrderDao().deleteByShop(shop.shop_id)
        et_search!!.text!!.clear()
        CreateOrderFragment.startFragmentWithValue("Shop", shop, ProductSelectFragment(), ACTIVITY)
    }

    override fun onResume() {
        super.onResume()
        Log.d("Order", "onResume Shop Select")
        ConfirmOrderFragment.checkforOrders(queue!!, token!!, user_id!!, routeId!!)
    }
}