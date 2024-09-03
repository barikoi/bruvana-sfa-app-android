package com.barikoi.cnlapp.data.remote.api

import com.barikoi.cnlapp.data.remote.models.ApproveRequest
import com.barikoi.cnlapp.data.remote.models.BaseResponse
import com.barikoi.cnlapp.data.remote.models.DbHousesResponse
import com.barikoi.cnlapp.data.remote.models.OutletsResponse
import com.barikoi.cnlapp.data.remote.models.ProductStockResponse
import com.barikoi.cnlapp.data.remote.models.RequestStockResponse
import com.barikoi.cnlapp.data.remote.models.RouteResponse
import com.barikoi.cnlapp.data.remote.models.SoResponse
import com.barikoi.cnlapp.data.remote.models.StockRequestModel
import com.barikoi.cnlapp.data.remote.models.request.StockApprovalRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("api/v1/routes")
    suspend fun getRoute(@Query("user_id") userId: String): Response<RouteResponse>

    @GET("api/v1/routes")
    suspend fun getRouteWithOutlet(
        @Query("user_id") userId: String,
        @Query("with_outlets") filterWithOutlet: String
    ): Response<RouteResponse>

    @GET("api/v1/get-so")
    suspend fun getSoList(): Response<SoResponse>

    @GET("api/v1/outlets")
    suspend fun getOutlets(
        @Query("route_id") routeId: String,
        @Query("verified_outlets") isVerify: String,
        @Query("outlet_category") outletCategory: String
    ): Response<OutletsResponse>

    @GET("api/v1/user-requests")
    suspend fun getRequests(@Query("type") type: String): Response<RequestStockResponse>

    @POST("api/v1/user-request")
    suspend fun sendStockRequest(@Body body: StockRequestModel): Response<BaseResponse>

    @POST("api/v1/respond-user-request/{id}")
    suspend fun updateRequest(
        @Path("id") id: String,
        @Body approveRequest: ApproveRequest
    ): Response<BaseResponse>

    @POST("api/v1/respond-user-request/{id}")
    suspend fun updateRequest(
        @Path("id") id: String,
        @Body approveRequest: StockApprovalRequest?
    ): Response<BaseResponse>
    @GET("api/v1/db-houses")
    suspend fun getDHList(@Query("territory_id") territoryId: String): Response<DbHousesResponse>

    @GET("api/v1/db-houses")
    suspend fun sendStockRequest(@Query("territory_id") territoryId: String): Response<DbHousesResponse>

//    Api.all_product_list + "?start_date=" + EndDate + " 00:00:00" + "&end_date=" + EndDate + " 23:59:59" + "&with_stock=1&with_order=1" + territorySuffix
//    db_house_id
//    user_id

    @GET("api/v1/products")
    suspend fun getProductStock(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("with_stock") withStock: String?,
        @Query("with_order") withOrder: String?,
        @Query("db_house_id") dbHouseId: String?,
        @Query("user_id") userId: String?
    ): Response<ProductStockResponse>
}