package com.barikoi.cnlapp.data.remote.repository

import com.barikoi.cnlapp.BuildConfig
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.Failure
import com.barikoi.cnlapp.base.api.getErrorTypeByHTTPCode
import com.barikoi.cnlapp.data.remote.api.ApiService
import com.barikoi.cnlapp.data.remote.models.ReverseGeoResponse
import com.barikoi.cnlapp.data.remote.models.offer.OfferResponse
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.AppLogger
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.UnknownHostException
import javax.inject.Inject

interface ReverseGeoRepository {
    fun getReverseGeo(
        lat: String,
        lng: String
    ): Flow<ApiState<ReverseGeoResponse>>
}

class ReverseGeoRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ReverseGeoRepository {
    override fun getReverseGeo(
        lat: String,
        lng: String
    ): Flow<ApiState<ReverseGeoResponse>> {
        return flow {
            try {
                val response = apiService.getReverseGeo(
                    "${Api.reverseGeo}?key=${BuildConfig.TRACE_API_KEY}&latitude=$lat&longitude=$lng"
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