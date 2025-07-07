package com.barikoi.cnlapp.ui.ProductStock.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.models.BaseResponse
import com.barikoi.cnlapp.data.remote.models.DbHousesResponse
import com.barikoi.cnlapp.data.remote.models.ProductStockResponse
import com.barikoi.cnlapp.data.remote.models.StockRequestModel
import com.barikoi.cnlapp.data.remote.repository.ProductStockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductStockUpdateViewModel @Inject constructor(
    private val productStockRepository: ProductStockRepository
) : ViewModel() {

    private val _dbHousesResponse = MutableLiveData<ApiState<DbHousesResponse>>()
    val dbHousesResponse: LiveData<ApiState<DbHousesResponse>> = _dbHousesResponse

    private val _productResponse = MutableLiveData<ApiState<ProductStockResponse>>()
    val productResponse: LiveData<ApiState<ProductStockResponse>> = _productResponse


    private val _productStockRequestResponse = MutableLiveData<ApiState<BaseResponse>>()
    val productStockRequestResponse: LiveData<ApiState<BaseResponse>> = _productStockRequestResponse


    private val _soSelected = MutableLiveData<String>()
    val soSelected: LiveData<String> = _soSelected

    fun selectedRouted(soID: String) {
        _soSelected.postValue(soID)
    }

    fun getDHList(territoryId: String?, regionId: String?) {
        viewModelScope.launch {
            productStockRepository.getDHList(territoryId, regionId).onStart {
                _dbHousesResponse.postValue(ApiState.Loading())
            }.collectLatest {
                _dbHousesResponse.postValue(it)
            }
        }
    }

    fun sendRequest(body: StockRequestModel) {
        viewModelScope.launch {
            productStockRepository.sendStockRequest(body).onStart {
                _productStockRequestResponse.postValue(ApiState.Loading())
            }.collectLatest {
                _productStockRequestResponse.postValue(it)
            }
        }
    }

    fun getProductStock(
        startDate: String,
        endDate: String,
        withStock: String,
        withOrder: String,
        dbHouseId: String?,
        userId: String?
    ) {
        viewModelScope.launch {
            productStockRepository.getProductStock(
                startDate,
                endDate,
                withStock,
                withOrder,
                dbHouseId,
                userId
            ).onStart {
                _productResponse.postValue(ApiState.Loading())
            }.collectLatest {
                _productResponse.postValue(it)
            }
        }
    }
}