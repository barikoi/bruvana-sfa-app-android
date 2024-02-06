package com.barikoi.cnlapp.data.remote.api

import com.barikoi.cnlapp.data.remote.models.SocketGroupResponse
import com.barikoi.cnlapp.data.remote.models.SocketUserResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SocketApiService {
    @GET("company/groups")
    suspend fun getSocketGroups(
    ): Response<SocketGroupResponse>

    @GET("company/group/users")
    suspend fun getSocketUsersByGroup(
        @Query("group_id") groupId: String
    ): Response<SocketUserResponse>
}