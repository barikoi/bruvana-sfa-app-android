package com.barikoi.cnlapp.Fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NoConnectionError
import com.android.volley.Request.Method.GET
import com.android.volley.RequestQueue
import com.android.volley.TimeoutError
import com.android.volley.toolbox.StringRequest
import com.barikoi.cnlapp.Adapter.MarketListAdapter
import com.barikoi.cnlapp.Adapter.RouteListAdapter
import com.barikoi.cnlapp.Model.Markets
import com.barikoi.cnlapp.Model.Routes
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.MoreSpinner
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import io.sentry.Sentry
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException


class RouteFragment : Fragment() {
    //private var queue: RequestQueue? = null
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    //private var mContext: Context? = null
    //private var recylerView: RecyclerView? = null
    //public var arrayList: ArrayList<Routes>? = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_route, container, false)
        recylerView = view.findViewById(R.id.routelist)
        progressBar = view.findViewById(R.id.progress_bar3)
        spinner = view.findViewById(R.id.spinnerRoutes)
        //getAllRouteList()
        return view
    }

    companion object{
        var arrayList: ArrayList<Routes>? = ArrayList()
        var marketList: ArrayList<Markets>? = ArrayList()
        var routesList: ArrayList<String>? = ArrayList()
        var selectedRouteId: String?= null
        var recylerView: RecyclerView? = null
        var spinner: MoreSpinner? = null
        var progressBar: ProgressBar? = null
        var mContext: Context? = null
        var queue: RequestQueue? = null
        fun getAllRouteList(userId: String){
            arrayList!!.clear()
            routesList!!.clear()
            marketList!!.clear()
            progressBar!!.visibility = View.VISIBLE
            val request = StringRequest(GET,
                Api.routes_withfilter+"?user_id="+userId+"&with_outlets=1",
                { response ->
                    //success
                    Log.d("RouteFrag", response)
                    try {
                        progressBar!!.visibility = View.GONE
                        val data = JSONObject(response)
                        val routesArray = data.getJSONArray("routes")
                        for (i in 0 until routesArray.length()){
                            val route = routesArray.getJSONObject(i)
                            val route_id = route.getString("id")
                            val route_name = route.getString("route_name")
                            val route_code = route.getString("route_code")
                            val area_name = route.getString("area_name")
                            val territory_name = route.getString("territory_name")
                            //val outlet_count = route.getString("outlet_count")

                            val exists = arrayList!!.find {
                                it.route_code == route_code
                            }
                            Log.d("Route", "routes exist: " + exists)
                            if (exists == null) {
                                routesList!!.add(route_name)
                                arrayList!!.add(
                                    Routes(
                                        route_id,
                                        route_code,
                                        route_name,
                                        territory_name,
                                        area_name,
                                        "",
                                        ArrayList()
                                    )
                                )
                            }
                            val marketsArray = route.getJSONArray("market")
                            for (j in 0 until marketsArray.length()) {
                                val market = marketsArray.getJSONObject(j)
                                val market_name = market.getString("name")
                                val market_id = market.getString("id")
                                val outlet_count = market.getString("outlet_count")
                                marketList!!.add(
                                    Markets(
                                        route_id,
                                        route_code,
                                        route_name,
                                        market_id,
                                        market_name,
                                        territory_name,
                                        area_name,
                                        outlet_count,
                                        ArrayList()
                                    )
                                )
                            }
                        }

                        if (spinner != null) {
                            val adapter = ArrayAdapter(
                                mContext!!,
                                android.R.layout.simple_spinner_item, routesList!!
                            )
                            spinner!!.adapter = adapter

                        }

                        spinner!!.onItemSelectedListener = object :
                            AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>,
                                view: View, position: Int, id: Long
                            ) {
                                selectedRouteId = arrayList!!.get(position).id
                                //getAllMarketList(selectedRouteId!!)
                                val marketItems = ArrayList<Markets>()
                                for (i in 0 until marketList!!.size){
                                    if (selectedRouteId == marketList!![i].route_id){
                                        marketItems.add(marketList!![i])
                                    }
                                }
                                if (marketItems.size > 0) {
                                    val adapter = MarketListAdapter(marketItems)
                                    recylerView!!.setAdapter(adapter)
                                }
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {
                                //parent.lastVisiblePosition
                            }
                        }

                        //getAllMarketList(selectedRouteId!!)

                        /*val adapter = RouteListAdapter(arrayList!!)
                        recylerView!!.setAdapter(adapter)*/
                    }catch (e: JSONException) {
                        Sentry.captureException(e)
                        e.printStackTrace()
                    }

                },
                { error ->
                    //error
                    Log.d("error", error.toString())
                    progressBar!!.visibility = View.GONE
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
            request.retryPolicy = DefaultRetryPolicy(
                60 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            queue!!.add(request)
        }

        /*fun getAllMarketList(routeId: String){
            marketList!!.clear()
            progressBar!!.visibility = View.VISIBLE
            val request = StringRequest(GET,
                Api.market_list+"?user_id="+routeId,
                { response ->
                    //success
                    Log.d("RouteFrag", response)
                    try {
                        progressBar!!.visibility = View.GONE
                        val data = JSONObject(response)
                        val marketsArray = data.getJSONArray("markets")
                        for (i in 0 until marketsArray.length()){
                            val market = marketsArray.getJSONObject(i)
                            val route_id = market.getString("id")
                            val route_name = market.getString("route_name")
                            val route_code = market.getString("route_code")
                            val area_name = market.getString("area_name")
                            val territory_name = market.getString("territory_name")
                            val outlet_count = market.getString("outlet_count")

                            marketList!!.add(Markets(route_id, route_code, route_name, territory_name, area_name, outlet_count, ArrayList()))

                        }

                        val adapter = MarketListAdapter(marketList!!)
                        recylerView!!.setAdapter(adapter)
                    }catch (e: JSONException) {
                        Sentry.captureException(e)
                        e.printStackTrace()
                    }

                },
                { error ->
                    //error
                    Log.d("error", error.toString())
                    progressBar!!.visibility = View.GONE
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
        }*/
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        queue = RequestQueueSingleton.getInstance(context).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        editor = prefs!!.edit()
        /*token = prefs.getString("token", "")
        user_id = prefs.getString("user_id", "")*/
        mContext = context
    }
}