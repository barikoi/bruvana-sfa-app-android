package com.barikoi.cnlapp.Fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.repository.RouteRepository
import com.barikoi.cnlapp.data.remote.models.OutletsResponse
import com.barikoi.cnlapp.data.remote.models.RouteResponse
import com.barikoi.cnlapp.data.remote.models.SoResponse
import com.barikoi.cnlapp.data.remote.models.SocketGroupResponse
import com.barikoi.cnlapp.data.remote.models.TraceUserResponse
import com.barikoi.cnlapp.data.remote.repository.TraceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val routeRepository: RouteRepository,
    private val traceRepository: TraceRepository
) : ViewModel() {

    private val _routeResponse = MutableLiveData<ApiState<RouteResponse>>()
    val routeResponse: LiveData<ApiState<RouteResponse>> = _routeResponse


    private val _soResponse = MutableLiveData<ApiState<SoResponse>>()
    val soResponse: LiveData<ApiState<SoResponse>> = _soResponse


    private val _outletResponse = MutableLiveData<ApiState<OutletsResponse>>()
    val outletResponse: LiveData<ApiState<OutletsResponse>> = _outletResponse


    private val _socketGroupResponse = MutableLiveData<ApiState<SocketGroupResponse>>()
    val socketGroupResponse: LiveData<ApiState<SocketGroupResponse>> = _socketGroupResponse


    private val _socketUsersResponse = MutableLiveData<ApiState<TraceUserResponse>>()
    val socketUsersResponse: LiveData<ApiState<TraceUserResponse>> = _socketUsersResponse

    fun getSocketGroups() {
        viewModelScope.launch {
            traceRepository.getAllGroup()
                .onStart {
                    _socketGroupResponse.postValue(ApiState.Loading())
                }
                .collectLatest {
                    _socketGroupResponse.postValue(it)
                }
        }
    }

    fun getSocketUserByGroup(groupId: String) {
        viewModelScope.launch {
            traceRepository.getAllUsersByGroupID(groupId)
                .onStart {
                    _socketUsersResponse.postValue(ApiState.Loading())
                }
                .collectLatest {
                    _socketUsersResponse.postValue(it)
                }
        }
    }

    fun getSoList() {
        viewModelScope.launch {
            routeRepository.getSoList()
                .onStart {
                    _soResponse.postValue(ApiState.Loading())
                }
                .collectLatest {
                    _soResponse.postValue(it)
                }
        }
    }

    fun getRoutes(userID: String) {
        viewModelScope.launch {
            routeRepository.getRoutes(userID)
                .onStart {
                    _routeResponse.postValue(ApiState.Loading())
                }
                .collectLatest {
                    _routeResponse.postValue(it)
                }
        }
    }

    fun getOutletList(routeID: String, isVerify: String, outletCategory: String) {
        viewModelScope.launch {
            routeRepository.getOutlets(routeID, isVerify, outletCategory)
                .onStart {
                    _outletResponse.postValue(ApiState.Loading())
                }
                .collectLatest {
                    _outletResponse.postValue(it)
                }
        }
    }
}