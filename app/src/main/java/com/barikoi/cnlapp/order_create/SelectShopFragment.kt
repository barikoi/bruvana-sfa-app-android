package com.barikoi.cnlapp.order_create

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.data.remote.models.Outlet
import com.barikoi.cnlapp.data.remote.models.Route
import com.barikoi.cnlapp.databinding.FragmentSelectShopBinding
import com.barikoi.cnlapp.order_create.Adapter.AdapterSelectShop
import com.barikoi.cnlapp.order_create.vm.SelectShopViewModel
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.extension.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SelectShopFragment : Fragment() {
    private lateinit var binding: FragmentSelectShopBinding

    private val viewModel: SelectShopViewModel by viewModels()

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    private lateinit var adapter: AdapterSelectShop

    private var routes: List<Route> = emptyList()
    private var shopList: List<Outlet> = emptyList()
    private var filterShopList: MutableList<Outlet> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startRouteObserve()
        startShopObserve()
        viewModel.getRoutes(
            sharePrefUtils.getString(Api.USER_ID)!!
        )

        adapter = AdapterSelectShop { outlet ->
            if (outlet.orderedToday == 1) {
                toast("You have already ordered from this shop today.")
            } else {
                toast("Selected shop: ${outlet.outletName}")
            }
        }

        binding.rcvShopList.layoutManager = LinearLayoutManager(requireContext())
        binding.rcvShopList.adapter = adapter

        binding.spinnerRoutes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                viewModel.getShopList(
                    sharePrefUtils.getString(Api.USER_ID)!!,
                    routes[position].id.toString(),
                )
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                AppLogger.log(getString(R.string.nothing_selected))
            }
        }

        binding.tvSortTitle.setOnClickListener {
            val popup = PopupMenu(requireContext(), binding.tvSortTitle)
            popup.menuInflater.inflate(R.menu.sort_menu_outlet, popup.menu)
            popup.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener,
                PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    when (item.itemId) {
                        R.id.menu_ztoa -> {
                            shopList.sortedByDescending {
                                it.outletName
                            }
                            adapter.updateData(shopList)
                            binding.tvSortTitle.text = resources.getString(R.string.ztoa)
                        }

                        R.id.menu_atoz -> {
                            shopList.sortedBy {
                                it.outletName
                            }

                            adapter.updateData(shopList)
                            binding.tvSortTitle.text = resources.getString(R.string.atoz)
                        }
                    }
                    return true
                }
            })
            popup.show()
        }

        binding.tvFilter.setOnClickListener {
            val popup = PopupMenu(requireContext(), binding.tvFilter)
            popup.menuInflater.inflate(R.menu.filter_menu_outlets, popup.menu)
            popup.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener,
                PopupMenu.OnMenuItemClickListener {
                @SuppressLint("NotifyDataSetChanged")
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    when (item.itemId) {
                        R.id.menu_All -> {
                            adapter.updateData(shopList.sortedBy { it.outletName })
                        }

                        R.id.menu_A -> {
                            filterShopList.clear()
                            filterShopList.addAll(shopList)
                            try {
                                filterShopList.removeIf {
                                    !it.outletCategory[0].toString().equals("A", true)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            if (filterShopList.isNotEmpty()) {
                                adapter.updateData(filterShopList)
                            }
                        }

                        R.id.menu_B -> {
                            filterShopList.clear()
                            filterShopList.addAll(shopList)

                            try {
                                filterShopList.removeIf {
                                    !it.outletCategory[0].toString().equals("B", true)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            adapter.updateData(filterShopList)
                        }

                        R.id.menu_C -> {
                            filterShopList.clear()
                            filterShopList.addAll(shopList)
                            try {
                                filterShopList.removeIf {
                                    !it.outletCategory[0].toString().equals("C", true)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            adapter.updateData(filterShopList)
                        }

                        R.id.menu_D -> {
                            filterShopList.clear()
                            filterShopList.addAll(shopList)
                            try {
                                filterShopList.removeIf {
                                    !it.outletCategory[0].toString().equals("D", true)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            adapter.updateData(filterShopList)
                        }

                        R.id.menu_E -> {
                            filterShopList.clear()
                            filterShopList.addAll(shopList)
                            try {
                                filterShopList.removeIf {
                                    !it.outletCategory[0].toString().equals("E", true)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            adapter.updateData(filterShopList)
                        }

                        R.id.menu_F -> {
                            filterShopList.clear()
                            filterShopList.addAll(shopList)
                            try {
                                filterShopList.removeIf {
                                    !it.outletCategory[0].toString().equals("F", true)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            adapter.updateData(filterShopList)
                        }

                        R.id.menu_pharmacy -> {
                            filterShopList.clear()
                            filterShopList.addAll(shopList)
                            try {
                                filterShopList.removeIf {
                                    !it.outletCategory[0].toString().equals("P", true)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            adapter.updateData(filterShopList)
                        }

                        R.id.menu_mpharma -> {
                            filterShopList.clear()
                            filterShopList.addAll(shopList)
                            try {
                                filterShopList.removeIf {
                                    !it.outletCategory[0].toString().equals("M", true)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            adapter.updateData(filterShopList)
                        }

                        R.id.menu_warehouse -> {
                            filterShopList.clear()
                            filterShopList.addAll(shopList)
                            try {
                                filterShopList.removeIf {
                                    !it.outletCategory[0].toString().equals("W", true)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            adapter.updateData(filterShopList)
                        }

                        R.id.menu_no_order -> {
                            filterShopList.clear()
                            filterShopList.addAll(shopList)
                            filterShopList.removeIf {
                                it.isNoOrder != 1
                            }
                            adapter.updateData(filterShopList)
                        }

                        R.id.menu_ordered -> {
                            filterShopList.clear()
                            filterShopList.addAll(shopList)
                            filterShopList.removeIf {
                                it.orderedToday != 1
                            }
                            adapter.updateData(filterShopList)
                        }
                    }
                    return true
                }
            })
            popup.show()
        }
    }

    private fun startRouteObserve() {
        lifecycleScope.launch {
            viewModel.routeResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("startRouteObserve::Empty")
                    }

                    is ApiState.Error -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("startRouteObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        binding.progressBar.isVisible = true
                        AppLogger.log("startRouteObserve::Loading")
                    }

                    is ApiState.Success -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("startRouteObserve:: Success ${it.data}")

                        if (it.data?.routes?.isEmpty() == true) {
                            toast("No route found")
                        } else {
                            routes = it.data!!.routes

                            val toNameList =
                                it.data.routes.map { to -> to.routeName }.toMutableList()

                            val adapter = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_spinner_item,
                                toNameList.toMutableList()
                            )
                            binding.spinnerRoutes.adapter = adapter
                        }
                    }
                }
            }
        }
    }

    private fun startShopObserve() {
        lifecycleScope.launch {
            viewModel.shopsResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("startShopObserve::Empty")
                    }

                    is ApiState.Error -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("startShopObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        binding.progressBar.isVisible = true
                        AppLogger.log("startShopObserve::Loading")
                    }

                    is ApiState.Success -> {
                        binding.progressBar.isVisible = false
                        AppLogger.log("startShopObserve:: Success ${it.data!!.outlets}")

                        if (it.data.outlets.isEmpty()) {
                            toast("No shop found")
                            adapter.updateData(emptyList())
                        } else {
                            shopList = it.data.outlets

                            val distanceSorted = it.data.outlets
                                .sortedBy { it.orderedToday }
                                .sortedBy { it.orderedToday }
                                .sortedBy { it.isNoOrder }

                            adapter.updateData(distanceSorted)
                        }
                    }
                }
            }
        }
    }

}