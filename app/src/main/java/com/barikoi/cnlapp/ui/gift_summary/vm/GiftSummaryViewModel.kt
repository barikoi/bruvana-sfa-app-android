package com.barikoi.cnlapp.ui.gift_summary.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.models.PendingResponse
import com.barikoi.cnlapp.data.remote.models.SoResponseX
import com.barikoi.cnlapp.data.remote.models.TodaySummaryResponse
import com.barikoi.cnlapp.data.remote.repository.SoRepository
import com.barikoi.cnlapp.data.remote.repository.SummaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GiftSummaryViewModel @Inject constructor(
    private val summaryRepository: SummaryRepository,
    private val soRepository: SoRepository
) : ViewModel() {


    private val _toResponse = MutableLiveData<ApiState<TodaySummaryResponse>>()
    val toResponse: LiveData<ApiState<TodaySummaryResponse>> = _toResponse

    private val _soResponse = MutableLiveData<ApiState<SoResponseX>>()
    val soResponse: LiveData<ApiState<SoResponseX>> = _soResponse

    fun getToList() {
        viewModelScope.launch {
            summaryRepository.getTodaySummary(null, null, null).onStart {
                _toResponse.postValue(ApiState.Loading())
            }.collect { response ->
                _toResponse.postValue(response)
            }
        }

    }


    fun getSoList(userId: String) {
        viewModelScope.launch {
            soRepository.getSoByTo(userId).onStart {
                _soResponse.postValue(ApiState.Loading())
            }.collect { response ->
                _soResponse.postValue(response)
            }
        }

    }
}