package com.barikoi.cnlapp.ui.route

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.models.RouteResponse
import com.barikoi.cnlapp.data.remote.models.SoResponseX
import com.barikoi.cnlapp.data.remote.models.TodaySummaryResponse
import com.barikoi.cnlapp.data.remote.repository.RouteRepository
import com.barikoi.cnlapp.data.remote.repository.SoRepository
import com.barikoi.cnlapp.data.remote.repository.SummaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val routeRepository: RouteRepository,
    private val summaryRepository: SummaryRepository,
    private val soRepository: SoRepository
) : ViewModel() {

    private val _routeResponse = MutableLiveData<ApiState<RouteResponse>>()
    val routeResponse: LiveData<ApiState<RouteResponse>> = _routeResponse


    private val _soSelected = MutableLiveData<String>()
    val soSelected: LiveData<String> = _soSelected

    private val _toResponse = MutableLiveData<ApiState<TodaySummaryResponse>>()
    val toResponse: LiveData<ApiState<TodaySummaryResponse>> = _toResponse


    private val _soResponse = MutableLiveData<ApiState<SoResponseX>>()
    val soResponse: LiveData<ApiState<SoResponseX>> = _soResponse


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
                    _soResponse.value = ApiState.Loading()
                }
                .collectLatest {
                    _soResponse.value = it
                }
        }
    }

}