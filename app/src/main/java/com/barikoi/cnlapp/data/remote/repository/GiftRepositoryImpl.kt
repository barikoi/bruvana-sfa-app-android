package com.barikoi.cnlapp.data.remote.repository

import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.Failure
import com.barikoi.cnlapp.base.api.getErrorTypeByHTTPCode
import com.barikoi.cnlapp.data.remote.api.ApiService
import com.barikoi.cnlapp.data.remote.models.BaseResponse
import com.barikoi.cnlapp.data.remote.models.GIftSummaryResponse
import com.barikoi.cnlapp.data.remote.models.GiftResponse
import com.barikoi.cnlapp.utils.AppLogger
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.RequestBody
import java.net.UnknownHostException
import javax.inject.Inject

interface GiftRepository {
    fun getGifts(): Flow<ApiState<GiftResponse>>
    fun saveGifts(body: RequestBody): Flow<ApiState<BaseResponse>>
    fun getGiftSummary(
        userId: String,
        startDate: String?,
        endDate: String?
    ): Flow<ApiState<GIftSummaryResponse>>
}

class GiftRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : GiftRepository {
    override fun getGifts(): Flow<ApiState<GiftResponse>> {
        return flow {
            try {
                val response = apiService.getGifts()
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

    override fun saveGifts(body: RequestBody): Flow<ApiState<BaseResponse>> {
        return flow {
            try {
                val response = apiService.saveGifts(body)
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

    override fun getGiftSummary(
        userId: String,
        startDate: String?,
        endDate: String?
    ): Flow<ApiState<GIftSummaryResponse>> {
        return flow {
            try {
                val response = apiService.getGIftSummary(
                    userId = userId,
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
}