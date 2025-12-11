package com.barikoi.cnlapp.ui.summary_details.fragment.outlet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.models.OutletTypeSummaryResponse
import com.barikoi.cnlapp.data.remote.models.active.OverViewStatsResponse
import com.barikoi.cnlapp.data.remote.repository.SummaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class OutletTypeSummaryViewModel @Inject constructor(
    private val summaryRepository: SummaryRepository
) : ViewModel() {

    private val _soStatsResponse = MutableLiveData<ApiState<OverViewStatsResponse>>()
    val soStatsResponse: LiveData<ApiState<OverViewStatsResponse>> =
        _soStatsResponse

    private val _outletTypeSummaryResponse = MutableLiveData<ApiState<OutletTypeSummaryResponse>>()
    val outletTypeSummaryResponse: LiveData<ApiState<OutletTypeSummaryResponse>> =
        _outletTypeSummaryResponse


    fun getSOSummary(
        startDate: String,
        endDate: String,
        userId: String
    ) {
        viewModelScope.launch {
            summaryRepository.getSoSummary(
                startDate, endDate, userId
            ).onStart {
                _soStatsResponse.value = ApiState.Loading()
            }
                .collect {
                    _soStatsResponse.value = it
                }
        }
    }

    fun getOutletTypeSummary(
        startDate: String,
        endDate: String,
        userId: String
    ) {
        viewModelScope.launch {
            summaryRepository.getOutletTypeSummary(
                startDate, endDate, userId
            ).onStart {
                _outletTypeSummaryResponse.value = ApiState.Loading()
            }.collect {
                _outletTypeSummaryResponse.value = it
            }
        }
    }
}