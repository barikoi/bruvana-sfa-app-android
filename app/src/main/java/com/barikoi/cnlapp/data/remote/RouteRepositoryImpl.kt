package com.barikoi.cnlapp.data.remote

import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.Failure
import com.barikoi.cnlapp.base.api.getErrorTypeByHTTPCode
import com.barikoi.cnlapp.data.remote.models.OutletsResponse
import com.barikoi.cnlapp.data.remote.models.RouteResponse
import com.barikoi.cnlapp.data.remote.models.SoResponse
import com.barikoi.cnlapp.utils.AppLogger
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.UnknownHostException
import javax.inject.Inject

interface RouteRepository {
    fun getRoutes(userId: String): Flow<ApiState<RouteResponse>>
    fun getSoList(): Flow<ApiState<SoResponse>>

    fun getOutlets(
        routeID: String,
        isVerify: String,
        outletCategory: String
    ): Flow<ApiState<OutletsResponse>>
}

class RouteRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : RouteRepository {
    override fun getRoutes(userId: String): Flow<ApiState<RouteResponse>> {
        return flow {
            try {
                val response = apiService.getRoute(userId)
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

    override fun getSoList(): Flow<ApiState<SoResponse>> {
        return flow {
            try {
                val response = apiService.getSoList()
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

    override fun getOutlets(
        routeID: String,
        isVerify: String,
        outletCategory: String
    ): Flow<ApiState<OutletsResponse>> {
        return flow {
            try {
                val response = apiService.getOutlets(routeID, isVerify, outletCategory)
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