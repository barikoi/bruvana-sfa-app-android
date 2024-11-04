package com.barikoi.cnlapp.Activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.models.ApprovalCountResponse
import com.barikoi.cnlapp.data.remote.models.BaseResponse
import com.barikoi.cnlapp.data.remote.models.PendingResponse
import com.barikoi.cnlapp.data.remote.repository.ProductStockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val productStockRepository: ProductStockRepository
) : ViewModel() {

    private val _approvalCountResponse = MutableLiveData<ApiState<PendingResponse>>()
    val approvalCountResponse: LiveData<ApiState<PendingResponse>> = _approvalCountResponse

    fun getProductApprovalCount() {
        viewModelScope.launch {
            productStockRepository.getApprovalCount().collectLatest {
                _approvalCountResponse.value = it
            }
        }
    }
}