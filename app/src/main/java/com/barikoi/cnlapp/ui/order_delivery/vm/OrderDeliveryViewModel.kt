package com.barikoi.cnlapp.ui.order_delivery.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.models.OrderResponse
import com.barikoi.cnlapp.data.remote.models.SoResponse
import com.barikoi.cnlapp.data.remote.models.SoResponseX
import com.barikoi.cnlapp.data.remote.models.TodaySummaryResponse
import com.barikoi.cnlapp.data.remote.repository.OrderRepository
import com.barikoi.cnlapp.data.remote.repository.RouteRepository
import com.barikoi.cnlapp.data.remote.repository.SoRepository
import com.barikoi.cnlapp.data.remote.repository.SummaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderDeliveryViewModel @Inject constructor(
    private val summaryRepository: SummaryRepository,
    private val soRepository: SoRepository,
    private val routeRepository: RouteRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _toResponse = MutableLiveData<ApiState<TodaySummaryResponse>>()
    val toResponse: LiveData<ApiState<TodaySummaryResponse>> = _toResponse


    private val _soResponse = MutableLiveData<ApiState<SoResponseX>>()
    val soResponse: LiveData<ApiState<SoResponseX>> = _soResponse

    private val _orderResponse = MutableLiveData<ApiState<OrderResponse>>()
    val orderResponse: LiveData<ApiState<OrderResponse>> = _orderResponse

    private val _soListResponse = MutableLiveData<ApiState<SoResponse>>()
    val soListResponse: LiveData<ApiState<SoResponse>> = _soListResponse


    private val _soChangeObserve = MutableStateFlow("")
    val soChangeObserve: MutableStateFlow<String> = _soChangeObserve

    fun updateSoChange(soId: String) {
        _soChangeObserve.value = soId
    }

    private val _searchObserve = MutableStateFlow("")
    val searchObserve: MutableStateFlow<String> = _searchObserve

    fun updateSearchQuery(query: String) {
        _searchObserve.value = query
    }

    private val _dateRangeObserve = MutableStateFlow(Pair("", ""))
    val dateRangeObserve: MutableStateFlow<Pair<String, String>> = _dateRangeObserve

    fun updateDateRange(startDate: String, endDate: String) {
        _dateRangeObserve.value = Pair(startDate, endDate)
    }

    fun getTodaySummary(startDate: String, endDate: String, todaySummary: String) {
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
                    _soResponse.value = ApiState.Loading()
                }
                .collectLatest {
                    _soResponse.value = it
                }
        }
    }

    fun getSoList() {
        viewModelScope.launch {
            routeRepository.getSoList()
                .onStart {
                    _soListResponse.value = ApiState.Loading()
                }
                .collectLatest {
                    _soListResponse.value = it
                }
        }
    }

    fun getSavedOrders(
        startDate: String,
        endDate: String,
        userId: String,
        orderStatus: String,
        regionId: String?,
        territoryId: String?
    ) {
        viewModelScope.launch {
            orderRepository.getSavedOrders(
                startDate,
                endDate,
                userId,
                orderStatus,
                regionId,
                territoryId
            )
                .onStart {
                    _orderResponse.value = ApiState.Loading()
                }
                .collectLatest {
                    _orderResponse.value = it
                }
        }
    }
}