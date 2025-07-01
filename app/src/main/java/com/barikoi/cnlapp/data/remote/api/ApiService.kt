package com.barikoi.cnlapp.data.remote.api

import com.barikoi.cnlapp.data.remote.models.ApproveRequest
import com.barikoi.cnlapp.data.remote.models.AuthUserResponse
import com.barikoi.cnlapp.data.remote.models.BaseResponse
import com.barikoi.cnlapp.data.remote.models.BaseResponse2
import com.barikoi.cnlapp.data.remote.models.CheckAttendanceResponse
import com.barikoi.cnlapp.data.remote.models.DbHousesResponse
import com.barikoi.cnlapp.data.remote.models.GiftResponse
import com.barikoi.cnlapp.data.remote.models.LoginResponse
import com.barikoi.cnlapp.data.remote.models.NotificationResponse
import com.barikoi.cnlapp.data.remote.models.OutletsResponse
import com.barikoi.cnlapp.data.remote.models.PendingResponse
import com.barikoi.cnlapp.data.remote.models.ProductStockResponse
import com.barikoi.cnlapp.data.remote.models.RequestStockResponse
import com.barikoi.cnlapp.data.remote.models.ReverseGeoResponse
import com.barikoi.cnlapp.data.remote.models.RouteResponse
import com.barikoi.cnlapp.data.remote.models.SoResponse
import com.barikoi.cnlapp.data.remote.models.SoResponseX
import com.barikoi.cnlapp.data.remote.models.StockRequestModel
import com.barikoi.cnlapp.data.remote.models.TodaySummaryResponse
import com.barikoi.cnlapp.data.remote.models.offer.OfferResponse
import com.barikoi.cnlapp.data.remote.models.pre_order.PreviousDayOrderResponse
import com.barikoi.cnlapp.data.remote.models.product.ProductResponse
import com.barikoi.cnlapp.data.remote.models.request.StockApprovalRequest
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface ApiService {

    @POST("api/v1/generate-bpo-otp")
    suspend fun sendOtp(@Query("phone") mobile: String): Response<BaseResponse2>

    @POST("api/v1/login")
    suspend fun login(
        @Query("employee_id") employeeId: String, @Query("password") password: String
    ): Response<LoginResponse>

    @GET("api/v1/auth/user")
    suspend fun authUser(
        @Query("start_date") startDate: String?,
        @Query("end_date") endDate: String?,
        @Query("app_version") appVersion: String,
    ): Response<AuthUserResponse>

    @POST("api/v1/logout")
    suspend fun logout(): Response<BaseResponse2>

    @POST("api/v1/verify-bpo-otp")
    suspend fun verifyOTP(
        @Query("phone") mobile: String, @Query("otp") otp: String
    ): Response<BaseResponse>

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

    @GET("api/v1/outlets")
    suspend fun getPreviousDayOrder(
        @Query("user_id") userId: String?,
        @Query("outlet_id") outletId: String,
        @Query("with_last_week_order") withLastWeekOrder: String,
    ): Response<PreviousDayOrderResponse>

    @GET("api/v1/outlets")
    suspend fun getOutlets(
        @Query("user_id") userId: String,
        @Query("route_id") routeId: String,
    ): Response<OutletsResponse>

    @POST("api/v1/user-request")
    suspend fun sendStockRequest(@Body body: StockRequestModel): Response<BaseResponse>

    @GET("api/v1/user-requests")
    suspend fun getRequestStocksTO(
        @Query("type") type: String, @Query("requested_to") userID: String
    ): Response<RequestStockResponse>

    @GET("api/v1/user-requests")
    suspend fun getRequestStocksSO(
        @Query("type") type: String, @Query("user_id") userID: String
    ): Response<RequestStockResponse>

    @POST("api/v1/respond-user-request/{id}")
    suspend fun updateRequest(
        @Path("id") id: String,
        @Body approveRequest: ApproveRequest
    ): Response<BaseResponse>

    @GET("api/v1/notifications/{id}")
    suspend fun getNotifications(@Path("id") userId: String): Response<NotificationResponse>

    @FormUrlEncoded
    @POST("api/v1/notification/update")
    suspend fun readNotification(@Field("notification_id") notificationId: String): Response<BaseResponse2>

    @POST("api/v1/respond-user-request/{id}")
    suspend fun updateRequest(
        @Path("id") id: String,
        @Body approveRequest: StockApprovalRequest?
    ): Response<BaseResponse>

    @GET("api/v1/db-houses")
    suspend fun getDHList(
        @Query("territory_id") territoryId: String?,
        @Query("region_id") regionId: String?
    ): Response<DbHousesResponse>

    @GET("api/v1/db-houses")
    suspend fun sendStockRequest(@Query("territory_id") territoryId: String): Response<DbHousesResponse>

    @GET("api/v1/to-pending-count")
    suspend fun getApprovalCount(): Response<PendingResponse>

    @GET("api/v1/products")
    suspend fun getProductStock(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("with_stock") withStock: String?,
        @Query("with_order") withOrder: String?,
        @Query("db_house_id") dbHouseId: String?,
        @Query("user_id") userId: String?
    ): Response<ProductStockResponse>

    @GET("api/v1/gift-types")
    suspend fun getGifts(): Response<GiftResponse>

    @POST("api/v1/gift-history")
    suspend fun saveGifts(
        @Body body: RequestBody
    ): Response<BaseResponse>

    @GET("api/v1/get-to")
    suspend fun getTodaySummary(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("today_summary") todaySummary: String?
    ): Response<TodaySummaryResponse>

    @GET("api/v1/to-wise-so")
    suspend fun getSOByTO(
        @Query("to_id") toId: String,
    ): Response<SoResponseX>

    @POST("api/v1/create-order")
    suspend fun saveOrder(
        @Body body: RequestBody
    ): Response<BaseResponse>

    @POST("api/v1/no-orders")
    suspend fun saveNoOrder(
        @Body body: RequestBody
    ): Response<BaseResponse>

    @GET("api/v1/products")
    suspend fun getAllProducts(
        @Query("user_id") userId: String,
        @Query("with_stock") withStock: String = "1",
        @Query("is_active") withOrder: String = "1",
    ): Response<ProductResponse>

    @GET("api/v1/offers")
    suspend fun getOffers(
    ): Response<OfferResponse>

    @GET("api/v1/get-attendance")
    suspend fun checkAttendance(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
    ): Response<CheckAttendanceResponse>

    @GET
    suspend fun getReverseGeo(
        @Url url: String,
    ): Response<ReverseGeoResponse>
}