package com.barikoi.cnlapp.Activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.models.Route
import com.barikoi.cnlapp.data.remote.models.RouteResponse
import com.barikoi.cnlapp.data.remote.repository.RouteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val _routeResponse = MutableLiveData<ApiState<RouteResponse>>()
    val routeResponse: LiveData<ApiState<RouteResponse>> = _routeResponse


    private val _soSelected = MutableLiveData<String>()
    val soSelected: LiveData<String> = _soSelected


    fun selectedRouted(soID: String) {
        _soSelected.postValue(soID)
    }

    fun getRoutes(userID: String, filter: String) {
        viewModelScope.launch {
            routeRepository.getRouteWithOutlet(userID, filter).onStart {
                _routeResponse.postValue(ApiState.Loading())
            }.collectLatest {
                _routeResponse.postValue(it)
            }
        }
    }

}