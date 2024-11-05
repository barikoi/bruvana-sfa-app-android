package com.barikoi.cnlapp.base.di

import com.barikoi.cnlapp.data.remote.api.ApiService
import com.barikoi.cnlapp.data.remote.repository.RouteRepository
import com.barikoi.cnlapp.data.remote.repository.RouteRepositoryImpl
import com.barikoi.cnlapp.data.remote.api.TraceApiService
import com.barikoi.cnlapp.data.remote.repository.AuthRepository
import com.barikoi.cnlapp.data.remote.repository.AuthRepositoryImpl
import com.barikoi.cnlapp.data.remote.repository.NotificationRepository
import com.barikoi.cnlapp.data.remote.repository.NotificationRepositoryImpl
import com.barikoi.cnlapp.data.remote.repository.ProductStockRepository
import com.barikoi.cnlapp.data.remote.repository.ProductStockRepositoryImpl
import com.barikoi.cnlapp.data.remote.repository.SocketRepository
import com.barikoi.cnlapp.data.remote.repository.SocketRepositoryImpl
import com.barikoi.cnlapp.data.remote.repository.TraceRepository
import com.barikoi.cnlapp.data.remote.repository.TraceRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @Provides
    fun providesRouteRepository(apiService: ApiService): RouteRepository =
        RouteRepositoryImpl(apiService)

    @Provides
    fun providesSocketRepository(traceApiService: TraceApiService): SocketRepository =
        SocketRepositoryImpl(traceApiService)

    @Provides
    fun providesTraceRepository(traceApiService: TraceApiService): TraceRepository =
        TraceRepositoryImpl(traceApiService)

    @Provides
    fun providesProductStockRepository(apiService: ApiService): ProductStockRepository =
        ProductStockRepositoryImpl(apiService)

    @Provides
    fun providesNotificationRepository(apiService: ApiService): NotificationRepository =
        NotificationRepositoryImpl(apiService)

    @Provides
    fun providesAuthRepository(apiService: ApiService): AuthRepository =
        AuthRepositoryImpl(apiService)
}