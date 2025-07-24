package com.barikoi.cnlapp.data.remote.api

import android.content.Context
import android.content.Intent
import com.barikoi.cnlapp.BuildConfig
import com.barikoi.cnlapp.ui.auth.LoginActivity
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.Constants
import com.barikoi.cnlapp.utils.SharePrefUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val sharePrefUtils: SharePrefUtils,
    @ApplicationContext private val context: Context

) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val authorizedRequest = originalRequest
            .newBuilder()
            .header("Authorization", "bearer ${sharePrefUtils.getString(Constants.TOKEN)}")
            .build()

        val response = chain.proceed(authorizedRequest)

        AppLogger.log("REQUEST URL:: ${response.request.url}")

        if (response.code == 401 && !response.request.url.toString()
                .startsWith(BuildConfig.url_base + "api/v1/login")
        ) {
            AppLogger.log("response.code == 401:: ${response.message}")

            // Close the response body
            response.close()

            context.startActivity(
                Intent(
                    context,
                    LoginActivity::class.java
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })

            return response
        } else {
            return response
        }
    }
}