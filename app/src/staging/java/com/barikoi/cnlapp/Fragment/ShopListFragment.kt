package com.barikoi.cnlapp.Fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.TimeoutError
import com.android.volley.toolbox.StringRequest
import com.barikoi.cnlapp.Activity.CreateShopActivity
import com.barikoi.cnlapp.ui.route.RouteViewModel
import com.barikoi.cnlapp.Adapter.ShopListAdapter
import com.barikoi.cnlapp.Model.Routes
import com.barikoi.cnlapp.Model.Shops
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.callback.OnEditShopListener
import com.barikoi.cnlapp.databinding.FragmentShopListBinding
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.RequestQueueSingleton
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import io.sentry.Sentry
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import javax.inject.Inject

@AndroidEntryPoint
class ShopListFragment : Fragment(), OnEditShopListener {
    private lateinit var binding: FragmentShopListBinding

    private val viewModel: RouteViewModel by activityViewModels()

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    @Inject
    lateinit var queue: RequestQueue


    private var routesList: ArrayList<String>? = ArrayList()
    private var allRouteList: ArrayList<Routes>? = ArrayList()
    private var routeNameList: ArrayList<Pair<String, String>>? = ArrayList()
    private var shopList: ArrayList<Shops>? = ArrayList()

    private lateinit var adapter: ShopListAdapter

    private var userId: String? = ""
    private var listener: OnEditShopListener? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentShopListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        adapter = ShopListAdapter(ArrayList(), listener!!)

        binding.shoplist.adapter = adapter


        viewModel.soSelected.observe(viewLifecycleOwner) {
            userId = it
            getShopList(it)
        }

        if (sharePrefUtils.getString(Api.USER_TYPE).equals("SO")) {
            getShopList(sharePrefUtils.getString(Api.USER_ID)!!)
        }

        binding.spinnerRoutes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onItemSelected(
                parent: AdapterView<*>, view: View, position: Int, id: Long
            ) {
                sharePrefUtils.saveString(
                    Api.SELECTED_ROUTE_ID_LIST,
                    routeNameList!![position].first
                )
                sharePrefUtils.saveString(
                    Api.SELECTED_ROUTE_NAME_LIST, routeNameList!![position].second
                )
                val shops: ArrayList<Shops> = ArrayList()
                for (i in 0 until shopList!!.size) {
                    if (shopList!![i].route_name == routesList!![position]) {
                        shops.add(shopList!![i])
                    }
                }
                adapter = ShopListAdapter(shops, listener!!)
                binding.shoplist.adapter = adapter
                adapter.notifyDataSetChanged()

            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
                if (s!!.isEmpty()) {
                    val shops: ArrayList<Shops> = ArrayList()
                    if (shopList!!.size > 0) {
                        for (i in 0 until shopList!!.size) {
                            if (routesList!!.size > 0) {
                                if (shopList!![i].route_name == routesList!![binding.spinnerRoutes.selectedItemPosition]) {
                                    shops.add(shopList!![i])
                                }
                            }

                        }
                    }
                    adapter = ShopListAdapter(shops, listener!!)
                    binding.shoplist.adapter = adapter
                    adapter.notifyDataSetChanged()
                }

            }

            override fun afterTextChanged(s: Editable?) {}

        })

        val gd = GradientDrawable()
        gd.setColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        gd.cornerRadius = 5f
        gd.setStroke(
            2,
            ContextCompat.getColor(
                requireContext(),
                R.color.cnl_color_2
            )
        )
        binding.createShop.setBackgroundDrawable(gd)

        binding.createShop.setOnClickListener {
            if (routesList!!.size > 0) {
                startActivityResult.launch(
                    Intent(
                        requireActivity(),
                        CreateShopActivity::class.java
                    ).putExtra("requestCode", 55).putStringArrayListExtra("routes", routesList)
                        .putExtra("routeList", routeNameList)
                )
            } else {
                Toast.makeText(requireContext(), "Routes not Available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private var startActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == 55) {
            if (sharePrefUtils.getString(Api.USER_TYPE).equals("ASM") ||
                sharePrefUtils.getString(Api.USER_TYPE).equals("TO")
            ) {
                getShopList(userId!!)
            } else getShopList(sharePrefUtils.getString(Api.USER_ID)!!)
        }
    }


    private fun getShopList(userId: String) {
        binding.progressBar2.isVisible = true
        allRouteList!!.clear()
        queue = RequestQueueSingleton.getInstance(requireContext()).getRequestQueue()
        routesList!!.clear()
        val request = StringRequest(Request.Method.GET,
            Api.routes_withfilter + "?user_id=" + userId + "&with_outlets=1",
            { response ->
                Log.d("RouteFrag", response)
                try {
                    binding.progressBar2.isVisible = false
                    val data = JSONObject(response)
                    val routesArray = data.getJSONArray("routes")
                    shopList!!.clear()
                    routesList!!.clear()
                    routeNameList!!.clear()
                    for (i in 0 until routesArray.length()) {
                        val route = routesArray.getJSONObject(i)
                        val routeId = route.getString("id")
                        val routeName = route.getString("route_name")
                        val routeCode = route.getString("route_code")
                        val territoryName = route.getString("territory_name")
                        routesList!!.add(routeName)

                        val routeOutletList = route.getJSONArray("outlets")
                        for (j in 0 until routeOutletList.length()) {
                            val outlet = routeOutletList.getJSONObject(j)
                            var imageUrl = "null"
                            val imageList: ArrayList<String> = ArrayList()
                            if (outlet.has("images") && !outlet.isNull("images")) {
                                val imageArray = outlet.getJSONArray("images")
                                if (imageArray.length() > 0) {
                                    val imageObj = imageArray.getJSONObject(0)
                                    if (imageObj.has("image_url")) {
                                        imageUrl = imageObj.getString("image_url")
                                    }

                                    for (p in 0 until imageArray.length()) {
                                        val imageobj = imageArray.getJSONObject(p)
                                        if (imageobj.has("image_url")) {
                                            imageList.add(imageobj.getString("image_url"))
                                        }

                                    }
                                }

                            }
                            val outletId = outlet.getString("id")
                            val outletName = outlet.getString("outlet_name")
                            val outletStatus = outlet.getString("outlet_status")
                            val outletAddress = outlet.getString("address")
                            val outletCode = outlet.getString("outlet_code")
                            val outletType = outlet.getString("outlet_type")
                            val outletCategory = outlet.getString("outlet_category")
                            val ownerName = outlet.getString("owner_name")
                            val marketOpportunity = outlet.getString("market_opportunity")
                            val contactNumber = outlet.getString("phone_number")
                            val isBuyer = outlet.getInt("is_buyer")
                            val latitude = outlet.getDouble("latitude")
                            val longitude = outlet.getDouble("longitude")
                            val isVerified = outlet.getInt("is_verified")
                            val kitkatQS = outlet.getString("kitkat_qs")
                            val competitive: List<String>? = Gson().fromJson(
                                outlet.getString("competitive_products"),
                                object : TypeToken<List<String>>() {}.type
                            )

                            shopList!!.add(
                                Shops(
                                    outletId,
                                    outletName,
                                    outletStatus,
                                    outletAddress,
                                    outletCode,
                                    outletType,
                                    outletCategory,
                                    ownerName,
                                    "",
                                    marketOpportunity,
                                    contactNumber,
                                    isBuyer,
                                    imageUrl,
                                    imageList,
                                    territoryName,
                                    latitude,
                                    longitude,
                                    routeId,
                                    routeName,
                                    "",
                                    isVerified,
                                    0,
                                    0,
                                    0.0f,
                                    competitive,
                                    kitkatQS
                                )
                            )
                        }
                        Log.d("RouteList", "all 1 " + shopList!!.size.toString())
                        allRouteList!!.add(
                            Routes(
                                routeId, routeCode, routeName, "", "", "", shopList!!
                            )
                        )
                        routeNameList!!.add(
                            Pair(
                                routeId, routeName
                            )
                        )
                        binding.createShop.visibility = View.VISIBLE
                    }

                    val adapter = ArrayAdapter(
                        requireContext(), android.R.layout.simple_spinner_item, routesList!!
                    )
                    binding.spinnerRoutes.adapter = adapter


                } catch (e: JSONException) {
                    Sentry.captureException(e)
                    e.printStackTrace()
                }

            },
            { error ->
                binding.progressBar2.isVisible = false

                if (error is TimeoutError) {
                    Toast.makeText(
                        requireContext(),
                        "Request timeout!! Check your internet connection or Contact Admin",
                        Toast.LENGTH_LONG
                    ).show()
                }
                if (error is NoConnectionError) {
                    Toast.makeText(
                        requireContext(),
                        "Turn on your internet connection and Try again",
                        Toast.LENGTH_LONG
                    ).show()
                }
                if (error?.networkResponse != null) {
                    try {
                        val s = String(error.networkResponse.data)
                        Log.d("Verify", "message: $s")
                        val data = JSONObject(s)
                        Toast.makeText(
                            requireContext(),
                            data.getString("message"),
                            Toast.LENGTH_LONG
                        )
                            .show()
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                        Sentry.captureException(e)
                    } catch (e: JSONException) {
                        Sentry.captureException(e)
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                    }
                }
            })
        queue.add(request)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = this
    }

    override fun onEdit(shops: Shops) {
        if (routesList!!.size > 0) {
            startActivityResult.launch(
                Intent(requireActivity(), CreateShopActivity::class.java).putExtra(
                    "requestCode",
                    55
                ).putExtra("fromEdit", shops).putStringArrayListExtra("routes", routesList)
                    .putExtra("routeList", routeNameList)
            )
        } else {
            Toast.makeText(requireContext(), "Routes not Available", Toast.LENGTH_SHORT).show()
        }
    }
}