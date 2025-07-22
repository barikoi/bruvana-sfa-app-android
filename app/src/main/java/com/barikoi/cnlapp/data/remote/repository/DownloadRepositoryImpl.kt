package com.barikoi.cnlapp.data.remote.repository

import android.content.Context
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.Failure
import com.barikoi.cnlapp.data.remote.api.ApiService
import com.barikoi.cnlapp.data.remote.models.DownloadResponse
import com.barikoi.cnlapp.utils.FileUtils
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

interface DownloadRepository {
    suspend fun downloadChalan(
        orderIds: String,
        callback: (ApiState<DownloadResponse>) -> Unit
    )
}

class DownloadRepositoryImpl(
    private val apiService: ApiService,
    private val context: Context
) : DownloadRepository {

    override suspend fun downloadChalan(
        orderIds: String,
        callback: (ApiState<DownloadResponse>) -> Unit
    ) {
        val call = apiService.downloadChalans(
            orderIds
        )

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        try {
                            val fileName = FileUtils.saveFileToDownloads(
                                context,
                                body,
                                "Order Chalan",
                                "pdf"
                            )
                            callback(
                                ApiState.Success(
                                    DownloadResponse(
                                        "Chalan downloaded successfully",
                                        fileName
                                    )
                                )
                            )

                        } catch (e: Exception) {
                            e.printStackTrace()
                            callback(ApiState.Error(Failure.Exception(e)))
                        }
                    }
                        ?: callback(ApiState.Error(Failure.Exception(kotlin.Exception("Response body is null"))))
                } else {
                    callback(ApiState.Error(Failure.Exception(kotlin.Exception("Failed response: ${response.message()}"))))
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
                callback(ApiState.Error(Failure.Exception(t)))
            }
        })

    }

}