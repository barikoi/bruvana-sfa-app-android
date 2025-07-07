package com.barikoi.cnlapp.ui.home.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.models.active.ActiveInactiveUserResponse
import com.barikoi.cnlapp.data.remote.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository

) : ViewModel() {

    private val _activeInactiveResponse = MutableLiveData<ApiState<ActiveInactiveUserResponse>>()
    val activeInactiveResponse: LiveData<ApiState<ActiveInactiveUserResponse>> =
        _activeInactiveResponse

    fun getActiveInactiveUsers(
        startDate: String = "",
        endDate: String = ""
    ) {
        viewModelScope.launch {
            attendanceRepository.getActiveInactiveUsers(
                startDate = startDate,
                endDate = endDate
            )
                .onStart {
                    _activeInactiveResponse.value = ApiState.Loading()
                }
                .collect { response ->
                    _activeInactiveResponse.value = response
                }

        }
    }
}