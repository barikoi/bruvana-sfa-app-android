package com.barikoi.cnlapp.Activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.models.BaseResponse2
import com.barikoi.cnlapp.data.remote.models.LoginResponse
import com.barikoi.cnlapp.data.remote.repository.AuthRepository
import com.barikoi.cnlapp.utils.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginResponse = MutableLiveData<ApiState<LoginResponse>>()
    val loginResponse: LiveData<ApiState<LoginResponse>> = _loginResponse

    val userIdStateFlow = MutableStateFlow("")
    val passwordStateFlow = MutableStateFlow("")

    val isLoginInfoValid = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            combine(userIdStateFlow, passwordStateFlow) { userId, password ->
                isLoginInfoValid.value = userId.isNotEmpty() && password.isNotEmpty()
            }
                .collectLatest {
                    AppLogger.log("LoginViewModel:: Combine called")
                }
        }
    }

    fun login(employeeId: String, password: String) {
        viewModelScope.launch {
            authRepository.login(employeeId, password)
                .onStart {
                    _loginResponse.value = ApiState.Loading()
                }
                .collectLatest {
                    _loginResponse.value = it
                }

        }
    }

}