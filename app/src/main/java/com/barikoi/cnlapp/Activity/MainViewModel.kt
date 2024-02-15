package com.barikoi.cnlapp.Activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.models.trace.TraceLoginResponse
import com.barikoi.cnlapp.data.remote.models.trace.request.TraceLognRequest
import com.barikoi.cnlapp.data.remote.repository.SocketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.http.Body
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val socketRepository: SocketRepository,

) : ViewModel() {

    private val _loginResponse = MutableLiveData<ApiState<TraceLoginResponse>>()
    val loginResponse: LiveData<ApiState<TraceLoginResponse>> = _loginResponse


    fun traceLogin(traceLoginRequest: TraceLognRequest) {
        viewModelScope.launch {
            socketRepository.traceLogin(traceLoginRequest).collectLatest {
                _loginResponse.postValue(it)
            }
        }
    }
}