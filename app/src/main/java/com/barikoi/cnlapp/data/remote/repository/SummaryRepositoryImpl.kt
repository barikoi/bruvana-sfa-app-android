package com.barikoi.cnlapp.data.remote.repository

import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.Failure
import com.barikoi.cnlapp.base.api.getErrorTypeByHTTPCode
import com.barikoi.cnlapp.data.remote.api.ApiService
import com.barikoi.cnlapp.data.remote.models.OutletTypeSummaryResponse
import com.barikoi.cnlapp.data.remote.models.TodaySummaryResponse
import com.barikoi.cnlapp.data.remote.models.active.OverViewStatsResponse
import com.barikoi.cnlapp.data.remote.models.so.SoWithSummaryResponse
import com.barikoi.cnlapp.utils.AppLogger
import com.google.gson.Gson
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.UnknownHostException
import javax.inject.Inject

interface SummaryRepository {
    suspend fun getTodaySummary(
        startDate: String?, endDate: String?, todaySummary: String?
    ): Flow<ApiState<TodaySummaryResponse>>


    suspend fun getTOWithSummary(
        startDate: String?, endDate: String?
    ): Flow<ApiState<TodaySummaryResponse>>

    suspend fun getSoWithTodaySummary(
        startDate: String, endDate: String, toID: String
    ): Flow<ApiState<SoWithSummaryResponse>>

    suspend fun getOutletTypeSummary(
        startDate: String, endDate: String, userId: String
    ): Flow<ApiState<OutletTypeSummaryResponse>>


    suspend fun getSoSummary(
        startDate: String, endDate: String, userId: String
    ): Flow<ApiState<OverViewStatsResponse>>

    suspend fun getOverViewStatsTO(
        startDate: String,
        endDate: String,
        territoryId: String,
        userId: String
    ): Flow<ApiState<OverViewStatsResponse>>

    suspend fun getOverViewStatsASM(
        startDate: String,
        endDate: String,
        regionId: String,
        userId: String
    ): Flow<ApiState<OverViewStatsResponse>>
}

class SummaryRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : SummaryRepository {
    override suspend fun getTodaySummary(
        startDate: String?, endDate: String?, todaySummary: String?
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

    override suspend fun getTOWithSummary(
        startDate: String?,
        endDate: String?
    ): Flow<ApiState<TodaySummaryResponse>> {
        return flow {
            try {
                val response = apiService.getTOSummary(startDate, endDate)
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

    override suspend fun getSoWithTodaySummary(
        startDate: String,
        endDate: String,
        toID: String
    ): Flow<ApiState<SoWithSummaryResponse>> {
        return flow {
            try {
                val response = apiService.getSoWithTodaySummary(startDate, endDate, toID)
                if (response.isSuccessful) {
                    emit(ApiState.Success(response.body()!!))
                } else {
                    emit(
                        ApiState.Error(
                            getErrorTypeByHTTPCode(response.code()),
                            errorResponse = Gson().fromJson(
                                response.errorBody()?.string(), SoWithSummaryResponse::class.java
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

    override suspend fun getOutletTypeSummary(
        startDate: String,
        endDate: String,
        userId: String
    ): Flow<ApiState<OutletTypeSummaryResponse>> {
        return flow {
            try {
                val response = apiService.getOutletTypeSummary(startDate, endDate, userId)
                if (response.isSuccessful) {
                    emit(ApiState.Success(response.body()!!))
                } else {
                    emit(
                        ApiState.Error(
                            getErrorTypeByHTTPCode(response.code()),
                            errorResponse = Gson().fromJson(
                                response.errorBody()?.string(), OutletTypeSummaryResponse::class.java
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

    override suspend fun getSoSummary(
        startDate: String,
        endDate: String,
        userId: String
    ): Flow<ApiState<OverViewStatsResponse>> {
        return flow {
            try {
                val response = apiService.getSoSummary(startDate, endDate, userId)
                if (response.isSuccessful) {
                    emit(ApiState.Success(response.body()!!))
                } else {
                    emit(
                        ApiState.Error(
                            getErrorTypeByHTTPCode(response.code()),
                            errorResponse = Gson().fromJson(
                                response.errorBody()?.string(), OverViewStatsResponse::class.java
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

    override suspend fun getOverViewStatsTO(
        startDate: String,
        endDate: String,
        territoryId: String,
        userId: String
    ): Flow<ApiState<OverViewStatsResponse>> {
        return flow {
            try {
                val response =
                    apiService.getOverViewStatsTO(startDate, endDate, 1, territoryId, userId)
                if (response.isSuccessful) {
                    emit(ApiState.Success(response.body()!!))
                } else {
                    emit(
                        ApiState.Error(
                            getErrorTypeByHTTPCode(response.code()),
                            errorResponse = Gson().fromJson(
                                response.errorBody()?.string(), OverViewStatsResponse::class.java
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

    override suspend fun getOverViewStatsASM(
        startDate: String,
        endDate: String,
        regionId: String,
        userId: String
    ): Flow<ApiState<OverViewStatsResponse>> {
        return flow {
            try {
                val response = apiService.getOverViewStatsASM(
                    startDate, endDate,
                    1, regionId, userId
                )
                if (response.isSuccessful) {
                    emit(ApiState.Success(response.body()!!))
                } else {
                    emit(
                        ApiState.Error(
                            getErrorTypeByHTTPCode(response.code()),
                            errorResponse = Gson().fromJson(
                                response.errorBody()?.string(), OverViewStatsResponse::class.java
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