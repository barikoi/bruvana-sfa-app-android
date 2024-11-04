package com.barikoi.cnlapp.request

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.models.RequestStockResponse
import com.barikoi.cnlapp.data.remote.repository.ProductStockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class StockRequestViewModel @Inject constructor(
    private val productStockRepository: ProductStockRepository
) : ViewModel() {


    private val _requestsStockResponse = MutableLiveData<ApiState<RequestStockResponse>>()
    val requestsStockResponse: LiveData<ApiState<RequestStockResponse>> = _requestsStockResponse

    fun getStockRequests(type: String, userId: String) {
        viewModelScope.launch {
            productStockRepository.getRequestsSO(type, userId).onStart {
                _requestsStockResponse.value = ApiState.Loading()
            }.collect {
                _requestsStockResponse.value = it
            }
        }
    }

}