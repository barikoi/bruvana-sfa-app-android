package com.barikoi.cnlapp.data.remote.repository

import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.Failure
import com.barikoi.cnlapp.base.api.getErrorTypeByHTTPCode
import com.barikoi.cnlapp.data.remote.api.ApiService
import com.barikoi.cnlapp.data.remote.models.BaseResponse
import com.barikoi.cnlapp.data.remote.models.DbHousesResponse
import com.barikoi.cnlapp.data.remote.models.GiftResponse
import com.barikoi.cnlapp.data.remote.models.OutletsResponse
import com.barikoi.cnlapp.data.remote.models.PendingResponse
import com.barikoi.cnlapp.data.remote.models.ProductStockResponse
import com.barikoi.cnlapp.data.remote.models.RequestStockResponse
import com.barikoi.cnlapp.data.remote.models.SoResponse
import com.barikoi.cnlapp.data.remote.models.StockRequestModel
import com.barikoi.cnlapp.data.remote.models.request.StockApprovalRequest
import com.barikoi.cnlapp.utils.AppLogger
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.http.Query
import java.net.UnknownHostException
import javax.inject.Inject

interface AddGiftRepository {
    fun getGifts(): Flow<ApiState<GiftResponse>>
}

class AddGiftRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AddGiftRepository {
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
}