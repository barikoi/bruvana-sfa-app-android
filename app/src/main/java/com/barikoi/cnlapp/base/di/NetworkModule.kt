package com.barikoi.cnlapp.base.di

import android.content.Context
import com.android.volley.RequestQueue
import com.barikoi.cnlapp.BuildConfig
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.data.remote.api.ApiService
import com.barikoi.cnlapp.data.remote.api.SocketApiService
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.Api.TRACE_BASE_URL
import com.barikoi.cnlapp.utils.Constants.CNL_OK_CLIENT
import com.barikoi.cnlapp.utils.Constants.TRACE_OK_CLIENT
import com.barikoi.cnlapp.utils.RequestQueueSingleton
import com.barikoi.cnlapp.utils.SharePrefUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun getApiInterface(@Named("CNL_OK_CLIENT") retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideTraceApiInterface(@Named("TRACE_OK_CLIENT") retrofit: Retrofit): SocketApiService {
        return retrofit.create(SocketApiService::class.java)
    }

    @Singleton
    @Provides
    @Named(CNL_OK_CLIENT)
    fun provideCNOkClient(sharePrefUtils: SharePrefUtils): OkHttpClient {
        return OkHttpClient
            .Builder().apply {
                addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader(
                            "Authorization",
                            "Bearer ${sharePrefUtils.getString(Api.TOKEN)}"
                        )
                        .build()
                    chain.proceed(request)
                }
            }
            .callTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .addInterceptor(
                HttpLoggingInterceptor().setLevel(
                    if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
                )
            )
            .build()
    }

    @Singleton
    @Provides
    @Named(TRACE_OK_CLIENT)
    fun provideTraceOkClient(sharePrefUtils: SharePrefUtils): OkHttpClient {
        return OkHttpClient
            .Builder().apply {
                addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader(
                            "Authorization",
                            "Bearer ${sharePrefUtils.getString(Api.TRACE_TOKEN)}"
                        )
                        .build()
                    chain.proceed(request)
                }
            }
            .callTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .addInterceptor(
                HttpLoggingInterceptor().setLevel(
                    if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
                )
            )
            .build()
    }

    @Singleton
    @Provides
    @Named("CNL_OK_CLIENT")
    fun provideCNLRetrofitInstance(
        @Named(CNL_OK_CLIENT) okHttpClient: OkHttpClient,
        @ApplicationContext context: Context
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(context.getString(R.string.url_base))
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    @Named("TRACE_OK_CLIENT")
    fun provideTraceRetrofitInstance(
        @Named(TRACE_OK_CLIENT) okHttpClient: OkHttpClient,
        @ApplicationContext context: Context
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(TRACE_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    fun provideVolley(@ApplicationContext context: Context): RequestQueue {
        return RequestQueueSingleton.getInstance(context).requestQueue
    }
}