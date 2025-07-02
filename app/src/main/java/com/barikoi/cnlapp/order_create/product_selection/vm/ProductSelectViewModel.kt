package com.barikoi.cnlapp.order_create.product_selection.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.models.BaseResponse
import com.barikoi.cnlapp.data.remote.models.ReverseGeoResponse
import com.barikoi.cnlapp.data.remote.models.offer.Offer
import com.barikoi.cnlapp.data.remote.models.offer.OfferResponse
import com.barikoi.cnlapp.data.remote.models.pre_order.PreviousDayOrderResponse
import com.barikoi.cnlapp.data.remote.models.product.Product
import com.barikoi.cnlapp.data.remote.models.product.ProductResponse
import com.barikoi.cnlapp.data.remote.repository.OfferRepository
import com.barikoi.cnlapp.data.remote.repository.OrderRepository
import com.barikoi.cnlapp.data.remote.repository.ProductRepository
import com.barikoi.cnlapp.data.remote.repository.ReverseGeoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class ProductSelectViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
    private val offerRepository: OfferRepository,
    private val reverseGeoRepository: ReverseGeoRepository
) : ViewModel() {


    private val _previousDayResponse = MutableLiveData<ApiState<PreviousDayOrderResponse>>()
    val previousDayResponse: LiveData<ApiState<PreviousDayOrderResponse>> = _previousDayResponse

    private val _productResponse = MutableLiveData<ApiState<ProductResponse>>()
    val productResponse: LiveData<ApiState<ProductResponse>> = _productResponse


    private val _reverseGeoResponse = MutableLiveData<ApiState<ReverseGeoResponse>>()
    val reverseGeoResponse: LiveData<ApiState<ReverseGeoResponse>> = _reverseGeoResponse

    private val _offerResponse = MutableLiveData<ApiState<OfferResponse>>()
    val offerResponse: LiveData<ApiState<OfferResponse>> = _offerResponse

    fun getOffers() {
        viewModelScope.launch {
            offerRepository.getOffer()
                .onStart {
                    _offerResponse.postValue(ApiState.Loading())
                }
                .collect {
                    _offerResponse.postValue(it)
                }
        }
    }


    val products = MutableSharedFlow<List<Product>>()

    fun setProducts(product: List<Product>) {
        viewModelScope.launch {
            products.emit(product)
        }
    }

    val startOrderTime = MutableSharedFlow<String>()
    fun setOrderStartTime(date: String) {
        viewModelScope.launch {
            startOrderTime.emit(date)
        }
    }

    val offers = MutableSharedFlow<List<Offer>>()
    fun setOffers(offer: List<Offer>) {
        viewModelScope.launch {
            offers.emit(offer)
        }
    }

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

    fun getReverseGeo(lat: String, lng: String) {
        viewModelScope.launch {
            reverseGeoRepository.getReverseGeo(lat, lng)
                .onStart {
                    _reverseGeoResponse.postValue(ApiState.Loading())
                }
                .collect { response ->
                    if (response is ApiState.Success) {
                        // Handle success case
                        _reverseGeoResponse.postValue(response)
                    } else if (response is ApiState.Error) {
                        // Handle error case
                        _reverseGeoResponse.postValue(response)
                    }
                }
        }
    }

    fun getPreviousDayOrder(
        userId: String,
        orderId: String
    ) {
        viewModelScope.launch {
            orderRepository.getPreviousDayOrder(userId, orderId)
                .onStart {
                    _previousDayResponse.postValue(ApiState.Loading())
                }
                .collect {
                    _previousDayResponse.postValue(it)
                }
        }
    }

}