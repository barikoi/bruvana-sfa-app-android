package com.barikoi.cnlapp.ui.gift_summary.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.models.GIftSummaryResponse
import com.barikoi.cnlapp.data.remote.models.SoResponseX
import com.barikoi.cnlapp.data.remote.models.TodaySummaryResponse
import com.barikoi.cnlapp.data.remote.repository.GiftRepository
import com.barikoi.cnlapp.data.remote.repository.SoRepository
import com.barikoi.cnlapp.data.remote.repository.SummaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GiftSummaryViewModel @Inject constructor(
    private val summaryRepository: SummaryRepository,
    private val soRepository: SoRepository,
    private val giftRepository: GiftRepository
) : ViewModel() {

    private val _toResponse = MutableLiveData<ApiState<TodaySummaryResponse>>()
    val toResponse: LiveData<ApiState<TodaySummaryResponse>> = _toResponse

    private val _soResponse = MutableLiveData<ApiState<SoResponseX>>()
    val soResponse: LiveData<ApiState<SoResponseX>> = _soResponse

    private val _giftSummaryResponse = MutableLiveData<ApiState<GIftSummaryResponse>>()
    val giftSummaryResponse: LiveData<ApiState<GIftSummaryResponse>> = _giftSummaryResponse

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
    fun getGiftSummary(
        userId: String,
        startDate: String?,
        endDate: String?
    ) {
        viewModelScope.launch {
            giftRepository.getGiftSummary(userId, startDate, endDate).onStart {
                _giftSummaryResponse.postValue(ApiState.Loading())
            }.collect { response ->
                _giftSummaryResponse.postValue(response)
            }
        }
    }


}