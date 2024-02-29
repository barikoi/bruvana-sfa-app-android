package com.barikoi.cnlapp.Fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.model.socket.SocketGroupUsersResponse
import com.barikoi.cnlapp.data.remote.repository.RouteRepository
import com.barikoi.cnlapp.data.remote.models.OutletsResponse
import com.barikoi.cnlapp.data.remote.models.RouteResponse
import com.barikoi.cnlapp.data.remote.models.SoResponse
import com.barikoi.cnlapp.data.remote.models.SocketGroupResponse
import com.barikoi.cnlapp.data.remote.repository.SocketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val routeRepository: RouteRepository,
    private val socketRepository: SocketRepository
) : ViewModel() {

    private val _routeResponse = MutableLiveData<ApiState<RouteResponse>>()
    val routeResponse: LiveData<ApiState<RouteResponse>> = _routeResponse


    private val _soResponse = MutableLiveData<ApiState<SoResponse>>()
    val soResponse: LiveData<ApiState<SoResponse>> = _soResponse


    private val _outletResponse = MutableLiveData<ApiState<OutletsResponse>>()
    val outletResponse: LiveData<ApiState<OutletsResponse>> = _outletResponse


    private val _socketGroupResponse = MutableLiveData<ApiState<SocketGroupResponse>>()
    val socketGroupResponse: LiveData<ApiState<SocketGroupResponse>> = _socketGroupResponse


    private val _socketUsersResponse = MutableLiveData<ApiState<SocketGroupUsersResponse>>()
    val socketUsersResponse: LiveData<ApiState<SocketGroupUsersResponse>> = _socketUsersResponse

    fun getSocketGroups() {
        viewModelScope.launch {
            socketRepository.getAllGroup().collectLatest {
                _socketGroupResponse.postValue(it)
            }
        }
    }

    fun getSocketUserByGroup(groupId: String) {
        viewModelScope.launch {
            socketRepository.getAllUsersByGroupID(groupId).collectLatest {
                _socketUsersResponse.postValue(it)
            }
        }
    }

    fun getSoList() {
        viewModelScope.launch {
            routeRepository.getSoList().collectLatest {
                _soResponse.postValue(it)
            }
        }
    }

    fun getRoutes(userID: String) {
        viewModelScope.launch {
            routeRepository.getRoutes(userID).collectLatest {
                _routeResponse.postValue(it)
            }
        }
    }

    fun getOutletList(routeID: String, isVerify: String, outletCategory: String) {
        viewModelScope.launch {
            routeRepository.getOutlets(routeID, isVerify, outletCategory).collectLatest {
                _outletResponse.postValue(it)
            }
        }
    }
}