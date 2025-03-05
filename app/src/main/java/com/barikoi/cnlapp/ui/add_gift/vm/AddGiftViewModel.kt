package com.barikoi.cnlapp.ui.add_gift.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.models.BaseResponse
import com.barikoi.cnlapp.data.remote.models.GiftResponse
import com.barikoi.cnlapp.data.remote.repository.AddGiftRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class AddGiftViewModel @Inject constructor(
    private val addGiftRepository: AddGiftRepository
) : ViewModel() {


    private val _giftResponse = MutableLiveData<ApiState<GiftResponse>>()
    val giftResponse: LiveData<ApiState<GiftResponse>> = _giftResponse

    private val _saveGiftResponse = MutableLiveData<ApiState<BaseResponse>>()
    val saveGiftResponse: LiveData<ApiState<BaseResponse>> = _saveGiftResponse

    fun getGifts() {
        viewModelScope.launch {
            addGiftRepository.getGifts().onStart {
                _giftResponse.value = ApiState.Loading()
            }.collect {
                _giftResponse.value = it

            }
        }

    }

    fun saveGift(body: RequestBody) {
        viewModelScope.launch {
            addGiftRepository.saveGifts(body).onStart {
                _saveGiftResponse.value = ApiState.Loading()
            }.collect {
                _saveGiftResponse.value = it
            }
        }
    }
}