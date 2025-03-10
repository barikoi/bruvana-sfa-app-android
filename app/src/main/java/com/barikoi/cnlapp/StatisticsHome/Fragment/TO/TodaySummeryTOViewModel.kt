package com.barikoi.cnlapp.StatisticsHome.Fragment.TO

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.models.TodaySummaryResponse
import com.barikoi.cnlapp.data.remote.repository.SummaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class TodaySummeryTOViewModel @Inject constructor(
    private val summaryRepository: SummaryRepository
) : ViewModel() {

    private val _todaySummaryResponse = MutableLiveData<ApiState<TodaySummaryResponse>>()
    val todaySummaryResponse: LiveData<ApiState<TodaySummaryResponse>> = _todaySummaryResponse


    fun getTodaySummary(startDate: String, endDate: String, todaySummary: String) {
        viewModelScope.launch {
            summaryRepository.getTodaySummary(startDate, endDate, todaySummary)
                .onStart {
                    _todaySummaryResponse.value = ApiState.Loading()
                }
                .collectLatest {
                    _todaySummaryResponse.value = it
                }
        }
    }
}