package com.barikoi.cnlapp.data.remote.repository

import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.Failure
import com.barikoi.cnlapp.base.api.getErrorTypeByHTTPCode
import com.barikoi.cnlapp.data.remote.api.ApiService
import com.barikoi.cnlapp.data.remote.models.SoResponseX
import com.barikoi.cnlapp.utils.AppLogger
import com.google.gson.Gson
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.UnknownHostException
import javax.inject.Inject

interface SoRepository {
    suspend fun getSoByTo(
        toID: String
    ): Flow<ApiState<SoResponseX>>
}

class SoRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : SoRepository {
    override suspend fun getSoByTo(
        toID: String
    ): Flow<ApiState<SoResponseX>> {
        return flow {
            try {
                val response = apiService.getSOByTO(toID)
                if (response.isSuccessful) {
                    emit(ApiState.Success(response.body()!!))
                } else {
                    emit(
                        ApiState.Error(
                            getErrorTypeByHTTPCode(response.code()),
                            errorResponse = Gson().fromJson(
                                response.errorBody()?.string(), SoResponseX::class.java
                            )
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