package com.barikoi.cnlapp.Activity


import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.android.volley.RequestQueue
import com.barikoi.cnlapp.Adapter.ViewPagerAdapter
import com.barikoi.cnlapp.Fragment.RouteFragment
import com.barikoi.cnlapp.Fragment.ShopListFragment
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.ac.BaseActivity
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.data.remote.models.SoUser
import com.barikoi.cnlapp.data.remote.models.To
import com.barikoi.cnlapp.databinding.ActivityRouteBinding
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.extension.formatDate
import com.barikoi.cnlapp.utils.extension.toast
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject


@AndroidEntryPoint
class RouteActivity : BaseActivity() {
    private lateinit var binding: ActivityRouteBinding

    private val viewModel: RouteViewModel by viewModels()

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    @Inject
    lateinit var mQueue: RequestQueue


    private var toList: List<To> = emptyList()
    var soListNew: List<SoUser> = emptyList()

    private var token: String? = ""

    var selectedSo: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startToObserve()
        startSOObserve()

        token = sharePrefUtils.getString(Api.TOKEN)

        if (sharePrefUtils.getString(Api.USER_TYPE).equals("TO")) {
            binding.spinnerLayoutTO.isVisible = false
            binding.spinnerLayoutSO2.visibility = View.VISIBLE

            viewModel.getSoByTo(
                sharePrefUtils.getString(Api.USER_ID).toString()
            )

        } else if (sharePrefUtils.getString(Api.USER_TYPE).equals("ASM")) {
            binding.spinnerLayoutTO.isVisible = true
            binding.spinnerLayoutSO2.visibility = View.VISIBLE

            viewModel.getTo(
                Calendar.getInstance().time.formatDate(),
                Calendar.getInstance().time.formatDate(),
                "0"
            )
        } else {
            binding.spinnerLayoutTO.visibility = View.GONE
            binding.spinnerLayoutSO2.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
        }

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val titles = arrayOf(
            resources.getString(R.string.list_tab_1), resources.getString(R.string.list_tab_2)
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
        } else {
            binding.viewPager.currentItem = 0
            binding.tvTitle.text = resources.getString(R.string.route_list)
        }

        binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("Fragment", "viewpager tab pos: $position")
                if (position == 0) {
                    binding.viewPager.currentItem = 0
                    sharePrefUtils.saveInt(Api.ROUTE_PAGE_SELECTED, 0)
                    binding.tvTitle.text = resources.getString(R.string.route_list)
                } else if (position == 1) {
                    binding.viewPager.currentItem = 1
                    sharePrefUtils.saveInt(Api.ROUTE_PAGE_SELECTED, 1)
                    binding.tvTitle.text = resources.getString(R.string.shop_list)
                }
            }
        })

        binding.spinnerTO.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                viewModel.getSoByTo(toList[p2].toId.toString())
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        binding.spinnerSO2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (binding.spinnerSO2.adapter.count > 0) {
                    viewModel.selectedRouted(soListNew[p2].id.toString())
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun startToObserve() {
        lifecycleScope.launch {
            viewModel.toResponse.observe(this@RouteActivity) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startToObserve::Empty")
                        binding.progressBar.isVisible = false
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startToObserve::Error ${it.error}")
                        binding.progressBar.isVisible = false

                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startToObserve::Loading")
                        binding.progressBar.isVisible = false
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startToObserve:: Success ${it.data}")
                        binding.progressBar.isVisible = false

                        if (it.data?.toList.isNullOrEmpty()) {
                            toast("To list empty")
                            return@observe
                        }

                        toList = it.data?.toList ?: emptyList()
                        val toNameList = it.data?.toList?.map { to -> to.toName }!!.toMutableList()

                        val adapter = ArrayAdapter(
                            this@RouteActivity,
                            android.R.layout.simple_spinner_item,
                            toNameList.toMutableList()
                        )
                        binding.spinnerTO.adapter = adapter

                    }
                }
            }
        }
    }

    private fun startSOObserve() {
        lifecycleScope.launch {
            viewModel.soResponse.observe(this@RouteActivity) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startSOObserve::Empty")
                        binding.progressBar.isVisible = false
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startSOObserve::Error ${it.error}")
                        binding.progressBar.isVisible = false

                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startSOObserve::Loading")
                        binding.progressBar.isVisible = true
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startSOObserve:: Success ${it.data}")
                        binding.progressBar.isVisible = false

                        if (it.data?.users.isNullOrEmpty()) {
                            toast("So User is empty")
                            return@observe
                        }

                        soListNew = it.data?.users ?: emptyList()
                        val soNameList: MutableList<String> =
                            it.data?.users?.map { to -> to.userName }!!.toMutableList()

                        val adapter = ArrayAdapter(
                            this@RouteActivity,
                            android.R.layout.simple_spinner_item, soNameList.toMutableList()
                        )
                        binding.spinnerSO2.adapter = adapter
                    }
                }
            }
        }
    }
}