package com.barikoi.cnlapp.data.remote.repository

import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.Failure
import com.barikoi.cnlapp.base.api.getErrorTypeByHTTPCode
import com.barikoi.cnlapp.data.remote.api.ApiService
import com.barikoi.cnlapp.data.remote.models.BaseResponse
import com.barikoi.cnlapp.data.remote.models.OrderResponse
import com.barikoi.cnlapp.data.remote.models.pre_order.PreviousDayOrderResponse
import com.barikoi.cnlapp.utils.AppLogger
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.RequestBody
import java.net.UnknownHostException
import javax.inject.Inject

interface OrderRepository {
    fun saveOrder(body: RequestBody): Flow<ApiState<BaseResponse>>
    fun getPreviousDayOrder(
        userId: String,
        orderId: String,
    ): Flow<ApiState<PreviousDayOrderResponse>>

    fun saveNoOrder(
        body: RequestBody
    ): Flow<ApiState<BaseResponse>>

    fun getOrders(
        startDate: String,
        endDate: String,
        userId: String
    ): Flow<ApiState<OrderResponse>>
}

class OrderRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : OrderRepository {

    override fun saveOrder(body: RequestBody): Flow<ApiState<BaseResponse>> {
        return flow {
            try {
                val response = apiService.saveOrder(body)
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

    override fun getPreviousDayOrder(
        userId: String,
        orderId: String
    ): Flow<ApiState<PreviousDayOrderResponse>> {
        return flow {
            try {
                val response = apiService.getPreviousDayOrder(userId, orderId, "1")
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

    override fun saveNoOrder(
        body: RequestBody
    ): Flow<ApiState<BaseResponse>> {
        return flow {
            try {
                val response = apiService.saveNoOrder(body)
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

    override fun getOrders(
        startDate: String,
        endDate: String,
        userId: String
    ): Flow<ApiState<OrderResponse>> {
        return flow {
            try {
                val response = apiService.getOrders(startDate, endDate, userId)
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