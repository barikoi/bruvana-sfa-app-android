package com.barikoi.cnlapp.ui.notification.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.data.remote.models.BaseResponse2
import com.barikoi.cnlapp.data.remote.models.NotificationResponse
import com.barikoi.cnlapp.data.remote.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _notificationResponse = MutableLiveData<ApiState<NotificationResponse>>()
    val notificationResponse: LiveData<ApiState<NotificationResponse>> = _notificationResponse

    private val _readNotificationResponse = MutableLiveData<ApiState<BaseResponse2>>()
    val readNotificationResponse: LiveData<ApiState<BaseResponse2>> = _readNotificationResponse

    fun getNotifications(userId: String) {
        viewModelScope.launch {
            notificationRepository.getNotifications(userId)
                .onStart {
                    _notificationResponse.value = ApiState.Loading()
                }
                .collectLatest {
                    _notificationResponse.value = it
                }
        }
    }

    fun readNotification(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.readNotification(notificationId)
                .onStart {
                    _readNotificationResponse.value = ApiState.Loading()
                }
                .collectLatest {
                    _readNotificationResponse.value = it
                }
        }
    }
}