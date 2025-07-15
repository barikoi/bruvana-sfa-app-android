package com.barikoi.cnlapp.ui.home.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.models.BaseResponse
import com.barikoi.cnlapp.data.remote.models.ActiveInactiveUserResponse
import com.barikoi.cnlapp.data.remote.models.TodaySummaryResponse
import com.barikoi.cnlapp.data.remote.models.active.OverViewStatsResponse
import com.barikoi.cnlapp.data.remote.models.so.SoWithSummaryResponse
import com.barikoi.cnlapp.data.remote.repository.AttendanceRepository
import com.barikoi.cnlapp.data.remote.repository.SummaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val summaryRepository: SummaryRepository

) : ViewModel() {

    private val _activeInactiveResponse = MutableLiveData<ApiState<ActiveInactiveUserResponse>>()
    val activeInactiveResponse: LiveData<ApiState<ActiveInactiveUserResponse>> =
        _activeInactiveResponse


    private val _overViewStatsTOResponse = MutableLiveData<ApiState<OverViewStatsResponse>>()
    val overViewStatsTOResponse: LiveData<ApiState<OverViewStatsResponse>> =
        _overViewStatsTOResponse


    private val _overViewStatsASMResponse = MutableLiveData<ApiState<OverViewStatsResponse>>()
    val overViewStatsASMResponse: LiveData<ApiState<OverViewStatsResponse>> =
        _overViewStatsASMResponse


    private val _toWithTodaySummaryResponse = MutableLiveData<ApiState<TodaySummaryResponse>>()
    val toWithTodaySummaryResponse: LiveData<ApiState<TodaySummaryResponse>> =
        _toWithTodaySummaryResponse

    private val _soWithTodaySummaryResponse = MutableLiveData<ApiState<SoWithSummaryResponse>>()
    val soWithTodaySummaryResponse: LiveData<ApiState<SoWithSummaryResponse>> =
        _soWithTodaySummaryResponse

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

    fun getOverViewStatsASM(
        startDate: String,
        endDate: String,
        regionId: String,
        userId: String
    ) {
        viewModelScope.launch {
            summaryRepository.getOverViewStatsASM(
                startDate = startDate,
                endDate = endDate,
                regionId = regionId,
                userId = userId
            )
                .onStart {
                    _overViewStatsTOResponse.value = ApiState.Loading()
                }
                .collect { response ->
                    _overViewStatsTOResponse.value = response
                }
        }
    }


    fun getOverViewStatsTO(
        startDate: String,
        endDate: String,
        territoryId: String,
        userId: String
    ) {
        viewModelScope.launch {
            summaryRepository.getOverViewStatsTO(
                startDate = startDate,
                endDate = endDate,
                territoryId = territoryId,
                userId = userId
            )
                .onStart {
                    _overViewStatsTOResponse.value = ApiState.Loading()
                }
                .collect { response ->
                    _overViewStatsTOResponse.value = response
                }
        }
    }

    fun getTOWIthTodaySummary(
        startDate: String? = null,
        endDate: String? = null
    ) {
        viewModelScope.launch {
            summaryRepository.getTodaySummary(
                startDate = startDate,
                endDate = endDate,
                todaySummary = "1"
            )
                .onStart {
                    _toWithTodaySummaryResponse.value = ApiState.Loading()
                }
                .collect { response ->
                    _toWithTodaySummaryResponse.value = response
                }
        }
    }

    fun getSOWIthTodaySummary(
        startDate: String,
        endDate: String,
        toId: String
    ) {
        viewModelScope.launch {
            summaryRepository.getSoWithTodaySummary(
                startDate = startDate,
                endDate = endDate,
                toID = toId
            )
                .onStart {
                    _soWithTodaySummaryResponse.value = ApiState.Loading()
                }
                .collect { response ->
                    _soWithTodaySummaryResponse.value = response
                }
        }
    }
}