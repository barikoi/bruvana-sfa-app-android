package com.barikoi.cnlapp.Activity


import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.Adapter.ViewPagerAdapter
import com.barikoi.cnlapp.Attendance.Model.SOList
import com.barikoi.cnlapp.Fragment.RouteFragment
import com.barikoi.cnlapp.Fragment.ShopListFragment
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.ac.BaseActivity
import com.barikoi.cnlapp.databinding.ActivityRouteBinding
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.utils.ApiService.ApiServices
import com.barikoi.cnlapp.utils.RequestQueueSingleton
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.ViewUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import javax.inject.Inject


@AndroidEntryPoint
class RouteActivity : BaseActivity() {
    private lateinit var binding: ActivityRouteBinding

    private val viewModel: RouteViewModel by viewModels()

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    var mQueue: RequestQueue? = null
    private var token: String? = ""

    private var srCode: String? = ""
    var selectedSo: Int? = null
    val soList: ArrayList<SOList> = ArrayList()

    companion object {
        var userId: String? = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        token = sharePrefUtils.getString(Api.TOKEN)

        if (sharePrefUtils.getString(Api.USER_TYPE).equals("TO") ||
            sharePrefUtils.getString(Api.USER_TYPE).equals("ASM")
        ) {
            userId = ""
            srCode = ""
        } else {
            userId = sharePrefUtils.getString(Api.USER_ID)
            srCode = sharePrefUtils.getString(Api.SR_CODE)
        }

        mQueue = RequestQueueSingleton.getInstance(applicationContext).requestQueue

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val titles = arrayOf(
            resources.getString(R.string.list_tab_1),
            resources.getString(R.string.list_tab_2)
        )

        val fragments = ArrayList<Fragment>()
        fragments.add(RouteFragment())
        fragments.add(ShopListFragment())
        binding.viewPager.adapter = ViewPagerAdapter(supportFragmentManager, lifecycle, fragments)

        TabLayoutMediator(
            binding.viewpagertab, binding.viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            tab.text = titles[position]
        }.attach()

        binding.viewPager.isUserInputEnabled = false

        if (sharePrefUtils.getInt(Api.ROUTE_PAGE_SELECTED) == 1) {
            binding.viewPager.currentItem = 1
            binding.tvTitle.text = resources.getString(R.string.shop_list)
            ShopListFragment.getShopList(userId!!)
        } else {
            binding.viewPager.currentItem = 0
            binding.tvTitle.text = resources.getString(R.string.route_list)
        }

        Log.d("Fragment", "viewpager current Item: " + binding.viewPager.currentItem)
        binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("Fragment", "viewpager tab pos: $position")
                if (position == 0) {
                    binding.viewPager.currentItem = 0
                    sharePrefUtils.saveInt(Api.ROUTE_PAGE_SELECTED, 0)
                    binding.tvTitle.text = resources.getString(R.string.route_list)
//                    RouteFragment.getAllRouteList(userId!!, RouteFragment.mListener!!)
                } else if (position == 1) {
                    binding.viewPager.currentItem = 1
                    sharePrefUtils.saveInt(Api.ROUTE_PAGE_SELECTED, 1)
                    binding.tvTitle.text = resources.getString(R.string.shop_list)
                    ShopListFragment.getShopList(userId!!)
                }
            }
        })

        if (sharePrefUtils.getString(Api.USER_TYPE).equals("TO", true) ||
            sharePrefUtils.getString(Api.USER_TYPE).equals("ASM", true)
        ) {
            binding.spinnerLayoutSO2.visibility = View.VISIBLE
            getSOList()
        } else {
            binding.spinnerLayoutSO2.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
        }
        binding.spinnerSO2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (binding.spinnerSO2.adapter.count > 0) {
                    selectedSo = p2
                    userId = soList[p2].id
                    srCode = soList[p2].employeeId
                    if (sharePrefUtils.getInt(Api.ROUTE_PAGE_SELECTED) == 0) {
                    } else if (sharePrefUtils.getInt(Api.ROUTE_PAGE_SELECTED) == 1) {
                        ShopListFragment.getShopList(userId!!)
                    }

                    viewModel.selectedRouted(soList[p2].id)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun getSOList() {
        ApiServices.apiGET(
            Api.get_all_so_list,
            mQueue!!, token!!, object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    viewSOList(response)
                    binding.progressBar.visibility = View.GONE
                }

                override fun onJSONResponseSuccess(response: JSONObject) {}

                override fun onNetworkResponseSuccess(response: NetworkResponse) {}

                override fun onResponseFailure(error: VolleyError) {
                    ViewUtils.getErrorResponse(error, applicationContext)
                    binding.progressBar.visibility = View.GONE
                }

                override fun onException(e: Exception) {
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                }
            })
    }

    private fun viewSOList(response: String) {
        try {
            soList.clear()
            val obj = JSONObject(response)
            val toArray = obj.getJSONArray("so_list")
            val soArray = toArray.getJSONObject(0).getJSONArray("sales_officers")
            val soNameList: ArrayList<String> = ArrayList()
            if (soArray.length() > 0) {
                for (i in 0 until soArray.length()) {
                    val soObj = soArray.getJSONObject(i)
                    var imageUrl = "null"
                    if (soObj.has("images") && !soObj.isNull("images")) {
                        val imageArray = soObj.getJSONArray("images")
                        if (imageArray.length() > 0) {
                            val imageobj = imageArray.getJSONObject(0)
                            if (imageobj.has("image_url")) {
                                imageUrl = imageobj.getString("image_url")
                            }
                        }
                    }
                    soList.add(
                        SOList(
                            soObj.getString("id"),
                            soObj.getString("user_name"),
                            soObj.getString("designation"),
                            if (soObj.has("employee_id")) soObj.getString("employee_id") else "",
                            imageUrl
                        )
                    )
                    soNameList.add(soObj.getString("user_name"))

                }
            }
            val adapter = ArrayAdapter(
                applicationContext,
                android.R.layout.simple_spinner_item, soNameList
            )
            binding.spinnerSO2.adapter = adapter
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}