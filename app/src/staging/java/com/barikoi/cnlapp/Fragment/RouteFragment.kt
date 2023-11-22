package com.barikoi.cnlapp.Fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.NoConnectionError
import com.android.volley.Request.Method.GET
import com.android.volley.RequestQueue
import com.android.volley.TimeoutError
import com.android.volley.toolbox.StringRequest
import com.barikoi.cnlapp.Adapter.RouteListAdapter
import com.barikoi.cnlapp.Model.Routes
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.RouteShopList.Callback.OnRouteFetchSuccess
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import io.sentry.Sentry
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException


class RouteFragment : Fragment(), OnRouteFetchSuccess {
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
        //getAllRouteList()
        return view
    }

    companion object{
        var arrayList: ArrayList<Routes>? = ArrayList()
        var recylerView: RecyclerView? = null
        var progressBar: ProgressBar? = null
        var mContext: Context? = null
        var queue: RequestQueue? = null
        var mListener: OnRouteFetchSuccess? = null
        fun getAllRouteList(userId: String, mCallback: OnRouteFetchSuccess){
            arrayList!!.clear()
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
                            val outlet_count = route.getString("outlet_count")

                            arrayList!!.add(Routes(route_id, route_code, route_name, territory_name, area_name, outlet_count, ArrayList()))

                        }

                        //mCallback.onSuccess(ArrayList(), arrayList, ArrayList())

                        val adapter = RouteListAdapter(arrayList!!)
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
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        queue = RequestQueueSingleton.getInstance(context).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        editor = prefs!!.edit()
        mListener = this
        /*token = prefs.getString("token", "")
        user_id = prefs.getString("user_id", "")*/
        mContext = context
    }

    override fun onSuccess(
        routeName: ArrayList<String>,
        routes: ArrayList<Routes>?,
        markets: ArrayList<Any>
    ) {

    }

    override fun onError(error: String) {

    }
}