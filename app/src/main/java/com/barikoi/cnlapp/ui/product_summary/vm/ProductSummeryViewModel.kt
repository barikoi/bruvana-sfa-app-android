package com.barikoi.cnlapp.ui.product_summary.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.models.DbHousesResponse
import com.barikoi.cnlapp.data.remote.models.ProductStockResponse
import com.barikoi.cnlapp.data.remote.repository.ProductStockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductSummeryViewModel @Inject constructor(
    private val productStockRepository: ProductStockRepository
) : ViewModel() {

    private val _dbHousesResponse = MutableLiveData<ApiState<DbHousesResponse>>()
    val dbHousesResponse: LiveData<ApiState<DbHousesResponse>> = _dbHousesResponse

    private val _productStockResponse = MutableLiveData<ApiState<ProductStockResponse>>()
    val productStockResponse: LiveData<ApiState<ProductStockResponse>> = _productStockResponse


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

    fun getProductStock(
        startDate: String,
        endDate: String,
        withStock: String?,
        withOrder: String?,
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
                _productStockResponse.postValue(ApiState.Loading())
            }.collectLatest {
                _productStockResponse.postValue(it)
            }
        }
    }
}