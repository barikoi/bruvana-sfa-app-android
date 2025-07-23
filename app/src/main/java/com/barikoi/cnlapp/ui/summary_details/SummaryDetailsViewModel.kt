package com.barikoi.cnlapp.ui.summary_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.models.DownloadResponse
import com.barikoi.cnlapp.data.remote.repository.DownloadRepository
import com.barikoi.cnlapp.data.remote.repository.OrderRepository
import com.barikoi.cnlapp.data.remote.repository.SummaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SummaryDetailsViewModel @Inject constructor(
    private val summaryRepository: SummaryRepository,
    private val orderRepository: OrderRepository,
    private val downloadRepository: DownloadRepository
) : ViewModel() {

    private val _ordersResponse = MutableLiveData<ApiState<String>>()
    val ordersResponse: LiveData<ApiState<String>> =
        _ordersResponse


    private val _downloadResponse =
        MutableLiveData<ApiState<DownloadResponse>>(ApiState.Empty())

    val downloadResponse: LiveData<ApiState<DownloadResponse>> =
        _downloadResponse


    fun downloadChalan(
        startDate: String,
        endDate: String,
        userId: String
    ) {
        viewModelScope.launch {
            orderRepository.getOrders(
                startDate, endDate, userId
            ).onStart {
                _downloadResponse.value = ApiState.Loading()
            }.collect {
                downloadChalan(
                    it.data!!.orders.joinToString(",") { order -> order.orderNo }
                )
            }
        }
    }

    private fun downloadChalan(orderIds: String) {
        viewModelScope.launch {
            downloadRepository.downloadChalan(orderIds) {
                _downloadResponse.value = it
            }
        }
    }
}