package com.barikoi.cnlapp.Fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.NoConnectionError
import com.android.volley.Request.Method.GET
import com.android.volley.RequestQueue
import com.android.volley.TimeoutError
import com.android.volley.toolbox.StringRequest
import com.barikoi.cnlapp.Activity.RouteViewModel
import com.barikoi.cnlapp.Adapter.RouteListAdapter
import com.barikoi.cnlapp.Model.Routes
import com.barikoi.cnlapp.RouteShopList.Callback.OnRouteFetchSuccess
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.databinding.FragmentRouteBinding
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.RequestQueueSingleton
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.extension.toast
import dagger.hilt.android.AndroidEntryPoint
import io.sentry.Sentry
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import javax.inject.Inject

@AndroidEntryPoint
class RouteFragment : Fragment() {
    private lateinit var binding: FragmentRouteBinding

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    private lateinit var routeListAdapter: RouteListAdapter

    private val viewModel: RouteViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRouteBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        routeListAdapter = RouteListAdapter()

        binding.routelist.layoutManager = LinearLayoutManager(requireContext())
        binding.routelist.adapter = routeListAdapter



        if (sharePrefUtils.getString(Api.USER_TYPE).equals("SO")) {
            sharePrefUtils.getString(Api.USER_ID)?.let { viewModel.getRoutes(it, "1") }
        }

        viewModel.soSelected.observe(viewLifecycleOwner) {
            viewModel.getRoutes(it, "1")
        }


        startRouteObserve()
    }

    private fun startRouteObserve() {
        lifecycleScope.launch {
            viewModel.routeResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        binding.progressBar3.isVisible = false
                        AppLogger.log("startRouteObserve::Empty")
                    }

                    is ApiState.Error -> {
                        binding.progressBar3.isVisible = false
                        AppLogger.log("startRouteObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startRouteObserve::Loading")
                        binding.progressBar3.isVisible = true
                    }

                    is ApiState.Success -> {
                        binding.progressBar3.isVisible = false
                        AppLogger.log("startRouteObserve:: Success ${it.data?.routes}")


                        routeListAdapter.setRouteListData(it.data?.routes!!)

                    }
                }
            }
        }
    }
}