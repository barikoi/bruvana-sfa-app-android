package com.barikoi.cnlapp.data.remote.repository

import com.barikoi.cnlapp.BuildConfig
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.Failure
import com.barikoi.cnlapp.base.api.getErrorTypeByHTTPCode
import com.barikoi.cnlapp.data.remote.api.ApiService
import com.barikoi.cnlapp.data.remote.models.TodaySummaryResponse
import com.barikoi.cnlapp.utils.AppLogger
import com.google.gson.Gson
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.UnknownHostException
import javax.inject.Inject

interface SummaryRepository {
    suspend fun getTodaySummary(
        startDate: String, endDate: String, todaySummary: String?
    ): Flow<ApiState<TodaySummaryResponse>>
}

class SummaryRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : SummaryRepository {
    override suspend fun getTodaySummary(
        startDate: String, endDate: String, todaySummary: String?
    ): Flow<ApiState<TodaySummaryResponse>> {
        return flow {
            try {
                val response = apiService.getTodaySummary(startDate, endDate, todaySummary)
                if (response.isSuccessful) {
                    emit(ApiState.Success(response.body()!!))
                } else {
                    emit(
                        ApiState.Error(
                            getErrorTypeByHTTPCode(response.code()),
                            errorResponse = Gson().fromJson(
                                response.errorBody()?.string(), TodaySummaryResponse::class.java
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