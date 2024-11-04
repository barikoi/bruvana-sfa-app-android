package com.barikoi.cnlapp.approval

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.models.BaseResponse
import com.barikoi.cnlapp.data.remote.models.RequestStockResponse
import com.barikoi.cnlapp.data.remote.models.request.StockApprovalRequest
import com.barikoi.cnlapp.data.remote.repository.ProductStockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class StockRequestApprovalViewModel @Inject constructor(
    private val productStockRepository: ProductStockRepository
) : ViewModel() {


    private val _requestsStockResponse = MutableLiveData<ApiState<RequestStockResponse>>()
    val requestsStockResponse: LiveData<ApiState<RequestStockResponse>> = _requestsStockResponse

    private val _updateStockRequestResponse = MutableLiveData<ApiState<BaseResponse>>()
    val updateStockRequestResponse: LiveData<ApiState<BaseResponse>> = _updateStockRequestResponse


    fun getRequestsStock(type: String, userId: String) {
        viewModelScope.launch {
            productStockRepository.getRequestsTO(type, userId).onStart {
                _requestsStockResponse.value = ApiState.Loading()
            }.collect {
                _requestsStockResponse.value = it
            }
        }
    }

    fun updateRequest(id: String, body: StockApprovalRequest?) {
        viewModelScope.launch {
            productStockRepository.updateRequest(id, body).onStart {
                _updateStockRequestResponse.value = ApiState.Loading()
            }.collect {
                _updateStockRequestResponse.value = it
            }
        }
    }
}