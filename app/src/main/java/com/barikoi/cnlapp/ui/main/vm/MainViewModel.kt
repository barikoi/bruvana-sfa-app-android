package com.barikoi.cnlapp.ui.main.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.models.BaseResponse2
import com.barikoi.cnlapp.data.remote.models.PendingResponse
import com.barikoi.cnlapp.data.remote.models.TraceLoginResponse
import com.barikoi.cnlapp.data.remote.repository.AuthRepository
import com.barikoi.cnlapp.data.remote.repository.ProductStockRepository
import com.barikoi.cnlapp.data.remote.repository.TraceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val productStockRepository: ProductStockRepository,
    private val traceRepository: TraceRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _approvalCountResponse = MutableLiveData<ApiState<PendingResponse>>()
    val approvalCountResponse: LiveData<ApiState<PendingResponse>> = _approvalCountResponse


    private val _traceLoginResponse = MutableLiveData<ApiState<TraceLoginResponse>>()
    val traceLoginResponse: LiveData<ApiState<TraceLoginResponse>> = _traceLoginResponse

    private val _traceAuthUserResponse = MutableLiveData<ApiState<TraceLoginResponse>>()
    val traceAuthUserResponse: LiveData<ApiState<TraceLoginResponse>> = _traceAuthUserResponse

    private val _logoutResponse = MutableLiveData<ApiState<BaseResponse2>>()
    val logoutResponse: LiveData<ApiState<BaseResponse2>> = _logoutResponse


    fun getProductApprovalCount() {
        viewModelScope.launch {
            productStockRepository.getApprovalCount().collectLatest {
                _approvalCountResponse.value = it
            }
        }
    }

    fun traceLogin(email: String, password: String) {
        viewModelScope.launch {
            traceRepository.traceLogin(email, password).collectLatest {
                _traceLoginResponse.value = it
            }
        }
    }

    fun traceAuthUser() {
        viewModelScope.launch {
            traceRepository.traceAuthUser().collectLatest {
                _traceAuthUserResponse.value = it
            }
        }
    }

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
}