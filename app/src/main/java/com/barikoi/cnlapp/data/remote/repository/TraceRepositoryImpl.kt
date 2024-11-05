package com.barikoi.cnlapp.data.remote.repository

import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.Failure
import com.barikoi.cnlapp.base.api.getErrorTypeByHTTPCode
import com.barikoi.cnlapp.data.remote.api.TraceApiService
import com.barikoi.cnlapp.data.remote.models.SocketGroupResponse
import com.barikoi.cnlapp.data.remote.models.TraceLoginResponse
import com.barikoi.cnlapp.data.remote.models.TraceUserResponse
import com.barikoi.cnlapp.data.remote.models.request.TraceLoginBody
import com.barikoi.cnlapp.utils.AppLogger
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.UnknownHostException
import javax.inject.Inject

interface TraceRepository {

    fun traceLogin(email: String, password: String): Flow<ApiState<TraceLoginResponse>>
    fun traceAuthUser(): Flow<ApiState<TraceLoginResponse>>

    fun getAllGroup(): Flow<ApiState<SocketGroupResponse>>
    fun getAllUsersByGroupID(groupId: String): Flow<ApiState<TraceUserResponse>>

}

class TraceRepositoryImpl @Inject constructor(private val traceApiService: TraceApiService) :
    TraceRepository {
    override fun traceLogin(email: String, password: String): Flow<ApiState<TraceLoginResponse>> {
        return flow {
            try {
                val response =
                    traceApiService.login(TraceLoginBody(email, password))
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

    override fun traceAuthUser(): Flow<ApiState<TraceLoginResponse>> {
        return flow {
            try {
                val response =
                    traceApiService.authLogin()
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
                    traceApiService.getSocketGroups()
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

    override fun getAllUsersByGroupID(groupId: String): Flow<ApiState<TraceUserResponse>> {
        return flow {
            try {
                val response =
                    traceApiService.getSocketUsersByGroup(groupId)
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