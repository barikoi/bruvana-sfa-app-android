package com.barikoi.cnlapp.data.remote.api

import com.barikoi.cnlapp.data.remote.models.SocketGroupResponse
import com.barikoi.cnlapp.data.remote.models.TraceLoginResponse
import com.barikoi.cnlapp.data.remote.models.TraceUserResponse
import com.barikoi.cnlapp.data.remote.models.request.TraceLoginBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TraceApiService {

    @POST("/v2/pre-login")
    suspend fun login(
        @Body body: TraceLoginBody,
    ): Response<TraceLoginResponse>

    @GET("/v2/auth/user")
    suspend fun authLogin(
    ): Response<TraceLoginResponse>


    @GET("/company/get-groups/details")
    suspend fun getSocketGroups(
    ): Response<SocketGroupResponse>

    @GET("/v2/company/get-group/{group_id}")
    suspend fun getSocketUsersByGroup(
        @Path("group_id") groupId: String
    ): Response<TraceUserResponse>
}