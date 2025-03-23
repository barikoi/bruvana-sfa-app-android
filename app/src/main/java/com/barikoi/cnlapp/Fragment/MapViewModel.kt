package com.barikoi.cnlapp.Fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.repository.RouteRepository
import com.barikoi.cnlapp.data.remote.models.OutletsResponse
import com.barikoi.cnlapp.data.remote.models.RouteResponse
import com.barikoi.cnlapp.data.remote.models.SoResponseX
import com.barikoi.cnlapp.data.remote.models.SocketGroupResponse
import com.barikoi.cnlapp.data.remote.models.TodaySummaryResponse
import com.barikoi.cnlapp.data.remote.models.TraceUserResponse
import com.barikoi.cnlapp.data.remote.repository.SoRepository
import com.barikoi.cnlapp.data.remote.repository.SummaryRepository
import com.barikoi.cnlapp.data.remote.repository.TraceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val routeRepository: RouteRepository,
    private val traceRepository: TraceRepository,
    private val summaryRepository: SummaryRepository,
    private val soRepository: SoRepository
) : ViewModel() {

    private val _routeResponse = MutableLiveData<ApiState<RouteResponse>>()
    val routeResponse: LiveData<ApiState<RouteResponse>> = _routeResponse

    private val _outletResponse = MutableLiveData<ApiState<OutletsResponse>>()
    val outletResponse: LiveData<ApiState<OutletsResponse>> = _outletResponse

    private val _socketGroupResponse = MutableLiveData<ApiState<SocketGroupResponse>>()
    val socketGroupResponse: LiveData<ApiState<SocketGroupResponse>> = _socketGroupResponse

    private val _socketUsersResponse = MutableLiveData<ApiState<TraceUserResponse>>()
    val socketUsersResponse: LiveData<ApiState<TraceUserResponse>> = _socketUsersResponse

    private val _toResponse = MutableLiveData<ApiState<TodaySummaryResponse>>()
    val toResponse: LiveData<ApiState<TodaySummaryResponse>> = _toResponse


    private val _soNewResponse = MutableLiveData<ApiState<SoResponseX>>()
    val soNewResponse: LiveData<ApiState<SoResponseX>> = _soNewResponse

    fun getTo(startDate: String, endDate: String, todaySummary: String) {
        viewModelScope.launch {
            summaryRepository.getTodaySummary(startDate, endDate, todaySummary)
                .onStart {
                    _toResponse.value = ApiState.Loading()
                }
                .collectLatest {
                    _toResponse.value = it
                }
        }
    }

    fun getSoByTo(toId: String) {
        viewModelScope.launch {
            soRepository.getSoByTo(toId)
                .onStart {
                    _soNewResponse.value = ApiState.Loading()
                }
                .collectLatest {
                    _soNewResponse.value = it
                }
        }
    }

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