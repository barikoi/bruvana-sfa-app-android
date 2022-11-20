package com.barikoi.cnlapp.Fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.TimeoutError
import com.android.volley.toolbox.StringRequest
import com.barikoi.cnlapp.Adapter.ShopListAdapter
import com.barikoi.cnlapp.Model.Routes
import com.barikoi.cnlapp.Model.Shops
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.MoreSpinner
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import io.sentry.Sentry
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException

class ShopListFragment : Fragment() {
    //private var queue: RequestQueue? = null
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var adapter: ShopListAdapter? = null
    private var userId: String? = ""
    //private var mContext: Context? = null
    //private var recylerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_shop_list, container, false)
        recylerView = view.findViewById(R.id.shoplist)
        spinner = view.findViewById(R.id.spinnerRoutes)
        progressBar2 = view.findViewById(R.id.progress_bar2)
        et_search = view.findViewById(R.id.etSearch)
        adapter = ShopListAdapter( ArrayList<Shops>())
        recylerView!!.adapter = adapter
        /*swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)

        swipeRefreshLayout.setOnRefreshListener(OnRefreshListener {
            swipeRefreshLayout.setRefreshing(false)
            editor!!.putInt(Api.ANNOUNCEMENT_PAGE_SELECTED, 0)
            editor!!.commit()
        })*/


        spinner!!.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {

                /*Toast.makeText(mContext,
                    getString(R.string.selected_item) + " " +
                            "" + languages[position], Toast.LENGTH_SHORT).show()*/
                /*for (i in 0 until allRouteList!!.size) {
                    Log.d("RouteList", allRouteList!![i].route_name +" "+routesList!![position])
                    Log.d("RouteList", allRouteList!![i].shopList.size.toString())
                    if (allRouteList!![i].route_name == routesList!![position]) {
                        Log.d("Route", "match: "+allRouteList!![i].route_name +" "+routesList!![position])
                        val adapter = ShopListAdapter(allRouteList!![i].shopList)
                        recylerView!!.setAdapter(adapter)
                        adapter.notifyDataSetChanged()
                        //break
                    }
                }*/
                val shops: ArrayList<Shops> = ArrayList()
                for (i in 0 until shopList!!.size) {
                    if (shopList!![i].route_name == routesList!![position]) {
                        shops.add(shopList!![i])
                    }

                }
                adapter!!.shopList=shops
                adapter!!.notifyDataSetChanged()

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }

        et_search!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                /*if (s!!.length>0){
                    adapter!!.filter.filter(s)
                }else{
                    getShopList(userId!!)
                }*/
                adapter!!.filter.filter(s)
                if (s!!.length == 0) {
                    val shops: ArrayList<Shops> = ArrayList()
                    for (i in 0 until shopList!!.size) {
                        if (shopList!![i].route_name == routesList!![spinner!!.selectedItemPosition]) {
                            shops.add(shopList!![i])
                        }

                    }
                    adapter!!.shopList=shops
                    adapter!!.notifyDataSetChanged()
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        return view
    }

    companion object{
        var routesList: ArrayList<String>? = ArrayList()
        var allRouteList: ArrayList<Routes>? = ArrayList()
        var shopList: ArrayList<Shops>? = ArrayList()
        var recylerView: RecyclerView? = null
        var progressBar2: ProgressBar? = null
        var mContext: Context? = null
        var queue: RequestQueue? = null
        var spinner : MoreSpinner? = null
        var et_search: AutoCompleteTextView? = null

        fun getShopList(userId: String){
            allRouteList!!.clear()
            //progressBar2!!.visibility = View.VISIBLE
            queue = RequestQueueSingleton.getInstance(mContext).getRequestQueue()
            routesList!!.clear()
            val request = StringRequest(
                Request.Method.GET,
                Api.route_outlet_list+"?sr_id="+userId,
                { response ->
                    //success
                    Log.d("RouteFrag", response)
                    try {
                        progressBar2!!.visibility = View.GONE
                        val data = JSONObject(response)
                        val routesArray = data.getJSONArray("so-routes")
                        shopList!!.clear()
                        routesList!!.clear()
                        for (i in 0 until routesArray.length()){
                            val route = routesArray.getJSONObject(i)
                            val route_id = route.getString("id")
                            val route_name = route.getString("route_name")
                            val route_code = route.getString("route_code")
                            val territory_name = route.getString("territory_name")
                            routesList!!.add(route_name)

                            val route_outlet_list = route.getJSONArray("outlets")
                            for (j in 0 until route_outlet_list.length()) {
                                val outlet = route_outlet_list.getJSONObject(j)
                                val outlet_id = outlet.getString("id")
                                val outlet_name = outlet.getString("outlet_name")
                                val outlet_status = outlet.getString("outlets_status")
                                val outlet_address = outlet.getString("address")
                                val outlet_code = outlet.getString("outlet_code")
                                val outlet_type = outlet.getString("store_type")
                                /*val outlet_category = outlet.getString("outlet_category")*/
                                val owner_name = outlet.getString("owner_name")
                                val distributor_office = outlet.getString("distributor_office")
                                val distributor_office_code = outlet.getString("distributor_office_code")
                                val latitude = outlet.getDouble("latitude")
                                val longitude = outlet.getDouble("longitude")
                                /*val last_order_date = outlet.getString("order_delivery_date")*/

                                shopList!!.add(
                                    Shops(
                                        outlet_id,
                                        outlet_name,
                                        outlet_status,
                                        outlet_address,
                                        outlet_code,
                                        outlet_type,
                                        "",
                                        owner_name,
                                        distributor_office,
                                        distributor_office_code,
                                        territory_name,
                                        latitude,
                                        longitude,
                                        route_id,
                                        route_name,
                                        "",
                                        0
                                    )
                                )
                            }
                            Log.d("RouteList", "all 1 "+shopList!!.size.toString())
                            allRouteList!!.add(Routes(route_id, route_code, route_name, "", "", "", shopList!!))
                            for (i in 0 until allRouteList!!.size) {
                                Log.d("RouteList", "all 2 "+allRouteList!![i].route_name+" "+allRouteList!![i].shopList.size.toString())
                            }
                        }
                        if (spinner != null) {
                            for (i in 0 until allRouteList!!.size) {
                                Log.d("RouteList", "all 3 "+allRouteList!![i].route_name+" "+allRouteList!![i].shopList.size.toString())
                            }
                            val adapter = ArrayAdapter(
                                mContext!!,
                                android.R.layout.simple_spinner_item, routesList!!
                            )
                            spinner!!.adapter = adapter

                        }

                    }catch (e: JSONException) {
                        Sentry.captureException(e)
                        e.printStackTrace()
                    }

                },
                { error ->
                    //error
                    Log.d("error", error.toString())
                    progressBar2!!.visibility = View.GONE
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
                            Log.d("Verify", "message: $s")
                            val data = JSONObject(s)
                            //Toast.makeText(mContext.getApplicationContext(), data.getString("message"), Toast.LENGTH_SHORT).show();
                            //mListerner.onFailure(data.getString("message"))
                            Toast.makeText(mContext, data.getString("message"), Toast.LENGTH_LONG).show()
                        } catch (e: UnsupportedEncodingException) {
                            e.printStackTrace()
                            Sentry.captureException(e)
                        } catch (e: JSONException) {
                            //mListerner.onFailure(e.message)
                            Sentry.captureException(e)
                            Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
                            e.printStackTrace()
                        }
                    }
                }
            )
            queue!!.add(request)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        //queue = RequestQueueSingleton.getInstance(context).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        editor = prefs!!.edit()
        //token = prefs.getString("token", "")
        userId = prefs!!.getString(Api.USER_ID, "")
        mContext = context
    }
}