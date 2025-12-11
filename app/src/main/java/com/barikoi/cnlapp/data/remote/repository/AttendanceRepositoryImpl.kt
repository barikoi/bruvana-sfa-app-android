package com.barikoi.cnlapp.data.remote.repository

import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.Failure
import com.barikoi.cnlapp.base.api.getErrorTypeByHTTPCode
import com.barikoi.cnlapp.data.remote.api.ApiService
import com.barikoi.cnlapp.data.remote.models.ActiveInactiveUserResponse
import com.barikoi.cnlapp.data.remote.models.CheckAttendanceResponse
import com.barikoi.cnlapp.utils.AppLogger
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.UnknownHostException
import javax.inject.Inject

interface AttendanceRepository {
    fun checkAttendance(
        startDate: String,
        endDate: String,
    ): Flow<ApiState<CheckAttendanceResponse>>


    fun getActiveInactiveUsers(
        startDate: String,
        endDate: String,
        userId: String
    ): Flow<ApiState<ActiveInactiveUserResponse>>
}

class AttendanceRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AttendanceRepository {
    override fun checkAttendance(
        startDate: String,
        endDate: String
    ): Flow<ApiState<CheckAttendanceResponse>> {
        return flow {
            try {
                val response = apiService.checkAttendance(
                    startDate = startDate,
                    endDate = endDate
                )
                if (response.isSuccessful) {
                    emit(ApiState.Success(response.body()!!))
                } else {
                    emit(
                        ApiState.Error(
                            getErrorTypeByHTTPCode(response.code())
                        )
                    )
                }
            } catch (exception: Throwable) {
                Sentry.captureException(exception)

                when (exception) {
                    is UnknownHostException -> {
                        emit(ApiState.Error((Failure.HTTP.NetworkConnection)))
                    }

                    else -> {
                        emit(ApiState.Error(Failure.Exception(exception)))
                    }
                }
                AppLogger.log(exception.toString())
            }
        }
    }

    override fun getActiveInactiveUsers(
        startDate: String,
        endDate: String,
        userId: String
    ): Flow<ApiState<ActiveInactiveUserResponse>> {
        return flow {
            try {
                val response = apiService.getActiveInactive(
                    startDate = startDate,
                    endDate = endDate,
                    userId
                )
                if (response.isSuccessful) {
                    emit(ApiState.Success(response.body()!!))
                } else {
                    emit(
                        ApiState.Error(
                            getErrorTypeByHTTPCode(response.code())
                        )
                    )
                }
            } catch (exception: Throwable) {
                Sentry.captureException(exception)

                when (exception) {
                    is UnknownHostException -> {
                        emit(ApiState.Error((Failure.HTTP.NetworkConnection)))
                    }

                    else -> {
                        emit(ApiState.Error(Failure.Exception(exception)))
                    }
                }
                AppLogger.log(exception.toString())
            }
        }
    }
}