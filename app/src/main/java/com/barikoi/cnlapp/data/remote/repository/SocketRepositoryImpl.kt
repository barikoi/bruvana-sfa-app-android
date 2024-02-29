package com.barikoi.cnlapp.data.remote.repository

import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.Failure
import com.barikoi.cnlapp.base.api.getErrorTypeByHTTPCode
import com.barikoi.cnlapp.data.model.socket.SocketGroupUsersResponse
import com.barikoi.cnlapp.data.remote.api.SocketApiService
import com.barikoi.cnlapp.data.remote.models.SocketGroupResponse
import com.barikoi.cnlapp.data.remote.models.trace.TraceLoginResponse
import com.barikoi.cnlapp.data.remote.models.trace.request.TraceLognRequest
import com.barikoi.cnlapp.utils.AppLogger
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.UnknownHostException
import javax.inject.Inject

interface SocketRepository {

    fun traceLogin(loginRequest: TraceLognRequest): Flow<ApiState<TraceLoginResponse>>
    fun getAllGroup(): Flow<ApiState<SocketGroupResponse>>
    fun getAllUsersByGroupID(groupId: String): Flow<ApiState<SocketGroupUsersResponse>>

}

class SocketRepositoryImpl @Inject constructor(private val socketApiService: SocketApiService) :
    SocketRepository {
    override fun traceLogin(loginRequest: TraceLognRequest): Flow<ApiState<TraceLoginResponse>> {
        return flow {
            try {
                val response =
                    socketApiService.traceLogin(loginRequest)
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

    override fun getAllGroup(): Flow<ApiState<SocketGroupResponse>> {
        return flow {
            try {
                val response =
                    socketApiService.getSocketGroups()
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

    override fun getAllUsersByGroupID(groupId: String): Flow<ApiState<SocketGroupUsersResponse>> {
        return flow {
            try {
                val response =
                    socketApiService.getSocketUsersByGroup(groupId)
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