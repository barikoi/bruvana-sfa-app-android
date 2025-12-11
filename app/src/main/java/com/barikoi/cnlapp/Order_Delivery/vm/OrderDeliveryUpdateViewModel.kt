package com.barikoi.cnlapp.Order_Delivery.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.models.BaseResponse2
import com.barikoi.cnlapp.data.remote.models.SoResponseX
import com.barikoi.cnlapp.data.remote.models.TodaySummaryResponse
import com.barikoi.cnlapp.data.remote.repository.AuthRepository
import com.barikoi.cnlapp.data.remote.repository.SoRepository
import com.barikoi.cnlapp.data.remote.repository.SummaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderDeliveryUpdateViewModel @Inject constructor(
    private val summaryRepository: SummaryRepository,
    private val soRepository: SoRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _toResponse = MutableLiveData<ApiState<TodaySummaryResponse>>()
    val toResponse: LiveData<ApiState<TodaySummaryResponse>> = _toResponse


    private val _soResponse = MutableLiveData<ApiState<SoResponseX>>()
    val soResponse: LiveData<ApiState<SoResponseX>> = _soResponse


    private val _logoutResponse = MutableLiveData<ApiState<BaseResponse2>>()
    val logoutResponse: LiveData<ApiState<BaseResponse2>> = _logoutResponse

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
                .onStart {
                    _logoutResponse.value = ApiState.Loading()
                }
                .collectLatest {
                    _logoutResponse.value = it
                }
        }
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
}