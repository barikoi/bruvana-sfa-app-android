package com.barikoi.cnlapp.base.di

import android.content.Context
import com.android.volley.RequestQueue
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.data.remote.ApiService
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.Constants
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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun getApiInterface(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Singleton
    @Provides
    fun getOkhttpClink(sharePrefUtils: SharePrefUtils): OkHttpClient {
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
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    }

    @Singleton
    @Provides
    fun getRetrofitInstance(
        okHttpClient: OkHttpClient,
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
    fun provideVolley(@ApplicationContext context: Context): RequestQueue {
        return RequestQueueSingleton.getInstance(context).requestQueue
    }
}