package com.barikoi.cnlapp.data.remote.api

import com.barikoi.cnlapp.data.model.socket.SocketGroupUsersResponse
import com.barikoi.cnlapp.data.remote.models.SocketGroupResponse
import com.barikoi.cnlapp.data.remote.models.SocketUserResponse
import com.barikoi.cnlapp.data.remote.models.trace.TraceLoginResponse
import com.barikoi.cnlapp.data.remote.models.trace.request.TraceLognRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SocketApiService {

    @POST("auth/login")
    suspend fun traceLogin(@Body body: TraceLognRequest): Response<TraceLoginResponse>

    @GET("company/groups")
    suspend fun getSocketGroups(
    ): Response<SocketGroupResponse>

    @GET("company/get-group/{group_id}")
    suspend fun getSocketUsersByGroup(
        @Path("group_id") groupId: String
    ): Response<SocketGroupUsersResponse>
}