package com.barikoi.cnlapp.data.remote.api

import com.barikoi.cnlapp.data.remote.models.OutletsResponse
import com.barikoi.cnlapp.data.remote.models.RouteResponse
import com.barikoi.cnlapp.data.remote.models.SoResponse
import retrofit2.Response
import retrofit2.http.GET
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
}