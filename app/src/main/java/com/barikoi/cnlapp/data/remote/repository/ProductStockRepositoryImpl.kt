package com.barikoi.cnlapp.data.remote.repository

import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.Failure
import com.barikoi.cnlapp.base.api.getErrorTypeByHTTPCode
import com.barikoi.cnlapp.data.remote.api.ApiService
import com.barikoi.cnlapp.data.remote.models.ApprovalCountResponse
import com.barikoi.cnlapp.data.remote.models.BaseResponse
import com.barikoi.cnlapp.data.remote.models.DbHousesResponse
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

interface ProductStockRepository {
    fun getDHList(territoryId: String): Flow<ApiState<DbHousesResponse>>


    fun getApprovalCount(): Flow<ApiState<PendingResponse>>
    fun sendStockRequest(body: StockRequestModel): Flow<ApiState<BaseResponse>>

    fun updateRequest(
        id: String,
        approveRequest: StockApprovalRequest?
    ): Flow<ApiState<BaseResponse>>

    fun getRequestsSO(type: String, userId: String): Flow<ApiState<RequestStockResponse>>
    fun getRequestsTO(type: String, userId: String): Flow<ApiState<RequestStockResponse>>
    fun getProductStock(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("with_stock") withStock: String?,
        @Query("with_order") withOrder: String?,
        @Query("db_house_id") dbHouseId: String?,
        @Query("user_id") userId: String?
    ): Flow<ApiState<ProductStockResponse>>

    fun getSoList(): Flow<ApiState<SoResponse>>

    fun getOutlets(
        routeID: String,
        isVerify: String,
        outletCategory: String
    ): Flow<ApiState<OutletsResponse>>
}

class ProductStockRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ProductStockRepository {

    override fun getRequestsSO(type: String, userId: String): Flow<ApiState<RequestStockResponse>> {
        return flow {
            try {
                val response = apiService.getRequestStocksSO(type, userId)
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

    override fun getRequestsTO(type: String, userId: String): Flow<ApiState<RequestStockResponse>> {
        return flow {
            try {
                val response = apiService.getRequestStocksTO(type, userId)
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

    override fun getDHList(territoryId: String): Flow<ApiState<DbHousesResponse>> {
        return flow {
            try {
                val response = apiService.getDHList(territoryId)
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

    override fun sendStockRequest(body: StockRequestModel): Flow<ApiState<BaseResponse>> {
        return flow {
            try {
                val response = apiService.sendStockRequest(body)
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

    override fun updateRequest(
        id: String,
        approveRequest: StockApprovalRequest?
    ): Flow<ApiState<BaseResponse>> {
        return flow {
            try {
                val response = apiService.updateRequest(id, approveRequest)
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

    override fun getApprovalCount(): Flow<ApiState<PendingResponse>> {
        return flow {
            try {
                val response = apiService.getApprovalCount()
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

    override fun getProductStock(
        startDate: String,
        endDate: String,
        withStock: String?,
        withOrder: String?,
        dbHouseId: String?,
        userId: String?
    ): Flow<ApiState<ProductStockResponse>> {
        return flow {
            try {
                val response = apiService.getProductStock(
                    startDate,
                    endDate,
                    withStock,
                    withOrder,
                    dbHouseId,
                    userId
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