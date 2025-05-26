package com.barikoi.cnlapp.Order_Create.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.models.BaseResponse
import com.barikoi.cnlapp.data.remote.models.product.ProductResponse
import com.barikoi.cnlapp.data.remote.repository.OrderRepository
import com.barikoi.cnlapp.data.remote.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import javax.inject.Inject


@HiltViewModel
class ProductSelectViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {


    private val _productResponse = MutableLiveData<ApiState<ProductResponse>>()
    val productResponse: LiveData<ApiState<ProductResponse>> = _productResponse


    private val _orderResponse = MutableLiveData<ApiState<BaseResponse>>()
    val orderResponse: LiveData<ApiState<BaseResponse>> = _orderResponse

    fun getProducts(userId: String) {
        viewModelScope.launch {
            productRepository.getAllProduct(userId)
                .onStart {
                    _productResponse.postValue(ApiState.Loading())
                }
                .collect {
                    _productResponse.postValue(it)
                }
        }
    }

    fun saveNoOrder(body: RequestBody) {
        viewModelScope.launch {
            orderRepository.saveNoOrder(body)
                .onStart {
                    _orderResponse.postValue(ApiState.Loading())
                }
                .collect {
                    _orderResponse.postValue(it)
                }
        }
    }

    fun saveOrder(body: RequestBody) {
        viewModelScope.launch {
            orderRepository.saveOrder(body)
                .onStart {
                    _orderResponse.postValue(ApiState.Loading())
                }
                .collect {
                    _orderResponse.postValue(it)
                }
        }
    }

}