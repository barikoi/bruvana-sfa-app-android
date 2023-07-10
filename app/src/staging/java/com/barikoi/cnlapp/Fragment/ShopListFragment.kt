package com.barikoi.cnlapp.Fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.TimeoutError
import com.android.volley.toolbox.StringRequest
import com.barikoi.cnlapp.Activity.CreateShopActivity
import com.barikoi.cnlapp.Activity.RouteActivity
import com.barikoi.cnlapp.Adapter.ShopListAdapter
import com.barikoi.cnlapp.Model.Routes
import com.barikoi.cnlapp.Model.Shops
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.MoreSpinner
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.callback.OnEditShopListener
import io.sentry.Sentry
import kotlinx.android.synthetic.main.fragment_shop_list.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException

class ShopListFragment : Fragment(), OnEditShopListener {
    //private var queue: RequestQueue? = null
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var adapter: ShopListAdapter? = null
    private var userId: String? = ""
    private var listener: OnEditShopListener? = null
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
        btncreateShop = view.findViewById(R.id.createShop)
        adapter = ShopListAdapter(ArrayList<Shops>(), listener!!)
        recylerView!!.adapter = adapter

        spinner!!.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {

                val shops: ArrayList<Shops> = ArrayList()
                for (i in 0 until shopList!!.size) {
                    if (shopList!![i].route_name == routesList!![position]) {
                        shops.add(shopList!![i])
                    }

                }
                adapter = ShopListAdapter(shops, listener!!)
                recylerView!!.adapter = adapter
                adapter!!.notifyDataSetChanged()

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                //parent.lastVisiblePosition
            }
        }

        et_search!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter!!.filter.filter(s)
                if (s!!.length == 0) {
                    val shops: ArrayList<Shops> = ArrayList()
                    if (shopList!!.size > 0) {
                        for (i in 0 until shopList!!.size) {
                            if (routesList!!.size > 0) {
                                if (shopList!![i].route_name == routesList!![spinner!!.selectedItemPosition]) {
                                    shops.add(shopList!![i])
                                }
                            }

                        }
                    }
                    adapter = ShopListAdapter(shops, listener!!)
                    recylerView!!.adapter = adapter
                    adapter!!.notifyDataSetChanged()
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gd = GradientDrawable()
        gd.setColor(mContext!!.resources.getColor(R.color.white))
        gd.cornerRadius = 5f
        gd.setStroke(2, mContext!!.resources.getColor(R.color.cnl_color_2))
        createShop.setBackgroundDrawable(gd)

        createShop.setOnClickListener {
            if (routesList!!.size> 0) {
                startActivityResult.launch(
                    Intent(requireActivity(), CreateShopActivity::class.java)
                        .putExtra("requestCode", 55)
                        .putStringArrayListExtra("routes", routesList)
                        .putExtra("routeList", routeNameList)
                )
            }else{
                Toast.makeText(mContext, "Routes not Available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var startActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            /*if (result.getResultCode() == Activity.RESULT_OK) {
                val intent = result.data
                if (intent!!.getIntExtra("requestCode", 0) == 55) {
                    getShopList(userId!!)
                }
            }*/
            if (result.getResultCode() == 55) {
                getShopList(RouteActivity.userId!!)
            }
        }
    )

    companion object {
        var routesList: ArrayList<String>? = ArrayList()
        var allRouteList: ArrayList<Routes>? = ArrayList()
        var routeNameList: ArrayList<Pair<String, String>>? = ArrayList()
        var shopList: ArrayList<Shops>? = ArrayList()
        var recylerView: RecyclerView? = null
        var progressBar2: ProgressBar? = null
        var mContext: Context? = null
        var queue: RequestQueue? = null
        var spinner: MoreSpinner? = null
        var btncreateShop: AppCompatButton? = null
        var et_search: AutoCompleteTextView? = null

        fun getShopList(userId: String) {
            allRouteList!!.clear()
            //progressBar2!!.visibility = View.VISIBLE
            queue = RequestQueueSingleton.getInstance(mContext).getRequestQueue()
            routesList!!.clear()
            val request = StringRequest(
                Request.Method.GET,
                Api.routes_withfilter + "?user_id=" + userId + "&with_outlets=1",
                { response ->
                    //success
                    Log.d("RouteFrag", response)
                    try {
                        progressBar2!!.visibility = View.GONE
                        val data = JSONObject(response)
                        val routesArray = data.getJSONArray("routes")
                        shopList!!.clear()
                        routesList!!.clear()
                        routeNameList!!.clear()
                        for (i in 0 until routesArray.length()) {
                            val route = routesArray.getJSONObject(i)
                            val route_id = route.getString("id")
                            val route_name = route.getString("route_name")
                            val route_code = route.getString("route_code")
                            val territory_name = route.getString("territory_name")
                            routesList!!.add(route_name)

                            val route_outlet_list = route.getJSONArray("outlets")
                            for (j in 0 until route_outlet_list.length()) {
                                val outlet = route_outlet_list.getJSONObject(j)
                                var imageUrl = "null"
                                var imageList: ArrayList<String> = ArrayList()
                                if (outlet.has("images") && !outlet.isNull("images")) {
                                    val imageArray = outlet.getJSONArray("images")
                                    if (imageArray.length() > 0) {
                                        val imageobj = imageArray.getJSONObject(0)
                                        if (imageobj.has("image_url")) {
                                            imageUrl = imageobj.getString("image_url")
                                        }

                                        for (p in 0 until imageArray.length()){
                                            val imageobj = imageArray.getJSONObject(p)
                                            if (imageobj.has("image_url")) {
                                                imageList.add(imageobj.getString("image_url"))
                                            }

                                        }
                                    }
                                }
                                val outlet_id = outlet.getString("id")
                                val outlet_name = outlet.getString("outlet_name")
                                val outlet_status = outlet.getString("outlet_status")
                                val outlet_address = outlet.getString("address")
                                val outlet_code = outlet.getString("outlet_code")
                                val outlet_type = outlet.getString("outlet_type")
                                val outlet_category = outlet.getString("outlet_category")
                                val owner_name = outlet.getString("owner_name")
                                val market_opportunity = outlet.getString("market_opportunity")
                                val contact_number = outlet.getString("phone_number")
                                val is_buyer = outlet.getInt("is_buyer")
                                /*val distributor_office = outlet.getString("distributor_office")
                                val distributor_office_code = outlet.getString("distributor_office_code")*/
                                val latitude = outlet.getDouble("latitude")
                                val longitude = outlet.getDouble("longitude")
                                val is_Verified = outlet.getInt("is_verified")
                                /*val last_order_date = outlet.getString("order_delivery_date")*/

                                shopList!!.add(
                                    Shops(
                                        outlet_id,
                                        outlet_name,
                                        outlet_status,
                                        outlet_address,
                                        outlet_code,
                                        outlet_type,
                                        outlet_category,
                                        owner_name,
                                        "",
                                        market_opportunity,
                                        contact_number,
                                        is_buyer,
                                        imageUrl,
                                        imageList,
                                        /*distributor_office,
                                        distributor_office_code,*/
                                        territory_name,
                                        latitude,
                                        longitude,
                                        route_id,
                                        route_name,
                                        "",
                                        is_Verified,0, 0
                                    )
                                )
                            }
                            Log.d("RouteList", "all 1 " + shopList!!.size.toString())
                            allRouteList!!.add(
                                Routes(
                                    route_id,
                                    route_code,
                                    route_name,
                                    "",
                                    "",
                                    "",
                                    shopList!!
                                )
                            )
                            routeNameList!!.add(
                                Pair(
                                    route_id,
                                    route_name
                                )
                            )
                            btncreateShop!!.visibility = View.VISIBLE
                        }
                        if (spinner != null) {
                            val adapter = ArrayAdapter(
                                mContext!!,
                                android.R.layout.simple_spinner_item, routesList!!
                            )
                            spinner!!.adapter = adapter

                        }

                    } catch (e: JSONException) {
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
                        Toast.makeText(
                            mContext,
                            "Request timeout!! Check your internet connection or Contact Admin",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    if (error is NoConnectionError) {
                        //mListerner.onFailure("Turn on your internet connection and Try again")
                        Toast.makeText(
                            mContext,
                            "Turn on your internet connection and Try again",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    if (error != null && error.networkResponse != null) {
                        try {
                            val s = String(error.networkResponse.data)
                            Log.d("Verify", "message: $s")
                            val data = JSONObject(s)
                            //Toast.makeText(mContext.getApplicationContext(), data.getString("message"), Toast.LENGTH_SHORT).show();
                            //mListerner.onFailure(data.getString("message"))
                            Toast.makeText(mContext, data.getString("message"), Toast.LENGTH_LONG)
                                .show()
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

    fun generateImages(imageArray: JSONArray){
        for(i in 0 until imageArray.length()){

        }
    }

    override fun onResume() {
        super.onResume()
        //getShopList(userId!!)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        //queue = RequestQueueSingleton.getInstance(context).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        editor = prefs!!.edit()
        //token = prefs.getString("token", "")
        userId = prefs!!.getString(Api.USER_ID, "")
        mContext = context
        listener = this
    }

    override fun onEdit(shops: Shops) {
        if (routesList!!.size> 0) {
            startActivityResult.launch(
                Intent(requireActivity(), CreateShopActivity::class.java)
                    .putExtra("requestCode", 55)
                    .putExtra("fromEdit", shops)
                    .putStringArrayListExtra("routes", routesList)
                    .putExtra("routeList", routeNameList)
            )
        }else{
            Toast.makeText(mContext, "Routes not Available", Toast.LENGTH_SHORT).show()
        }
    }
}