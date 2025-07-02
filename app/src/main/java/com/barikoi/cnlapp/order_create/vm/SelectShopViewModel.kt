package com.barikoi.cnlapp.order_create.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.models.BaseResponse
import com.barikoi.cnlapp.data.remote.models.CheckAttendanceResponse
import com.barikoi.cnlapp.data.remote.models.OutletsResponse
import com.barikoi.cnlapp.data.remote.models.RouteResponse
import com.barikoi.cnlapp.data.remote.repository.AttendanceRepository
import com.barikoi.cnlapp.data.remote.repository.RouteRepository
import com.barikoi.cnlapp.data.remote.repository.ShopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectShopViewModel @Inject constructor(
    private val routeRepository: RouteRepository,
    private val shopRepository: ShopRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    private val _routeResponse = MutableLiveData<ApiState<RouteResponse>>()
    val routeResponse: LiveData<ApiState<RouteResponse>> = _routeResponse

    private val _shopsResponse = MutableLiveData<ApiState<OutletsResponse>>()
    val shopsResponse: LiveData<ApiState<OutletsResponse>> = _shopsResponse

    private val _attendanceResponse = MutableLiveData<ApiState<CheckAttendanceResponse>>()
    val attendanceResponse: LiveData<ApiState<CheckAttendanceResponse>> = _attendanceResponse

    fun checkAttendance(
        startDate: String,
        endDate: String
    ) {
        viewModelScope.launch {
            attendanceRepository.checkAttendance(startDate, endDate)
                .onStart {
                    _attendanceResponse.value = ApiState.Loading()
                }
                .collect { state ->
                    _attendanceResponse.value = state
                }
        }
    }




    fun getRoutes(userId: String) {
        viewModelScope.launch {
            routeRepository.getRoutes(userId)
                .onStart {
                    _routeResponse.value = ApiState.Loading()
                }
                .collect { state ->
                    _routeResponse.value = state
                }
        }
    }

    fun getShopList(
        userId: String,
        routeId: String
    ) {
        viewModelScope.launch {
            shopRepository.getShopList(userId, routeId)
                .onStart {
                    _shopsResponse.value = ApiState.Loading()
                }
                .collect { state ->
                    _shopsResponse.value = state
                }
        }
    }
}