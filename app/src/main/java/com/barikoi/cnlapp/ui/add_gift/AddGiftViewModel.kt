package com.barikoi.cnlapp.ui.add_gift

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.models.GiftResponse
import com.barikoi.cnlapp.data.remote.repository.AddGiftRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddGiftViewModel @Inject constructor(
    private val addGiftRepository: AddGiftRepository
) : ViewModel() {


    private val _giftResponse = MutableLiveData<ApiState<GiftResponse>>()
    val routeResponse: LiveData<ApiState<GiftResponse>> = _giftResponse

    fun getGifts() {
        viewModelScope.launch {
            addGiftRepository.getGifts().onStart {
                _giftResponse.value = ApiState.Loading()
            }.collect {
                _giftResponse.value = it

            }
        }

    }
}