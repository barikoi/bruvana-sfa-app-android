package com.barikoi.cnlapp.base.di

import com.barikoi.cnlapp.data.remote.api.ApiService
import com.barikoi.cnlapp.data.remote.repository.RouteRepository
import com.barikoi.cnlapp.data.remote.repository.RouteRepositoryImpl
import com.barikoi.cnlapp.data.remote.api.TraceApiService
import com.barikoi.cnlapp.data.remote.repository.AddGiftRepository
import com.barikoi.cnlapp.data.remote.repository.AddGiftRepositoryImpl
import com.barikoi.cnlapp.data.remote.repository.AuthRepository
import com.barikoi.cnlapp.data.remote.repository.AuthRepositoryImpl
import com.barikoi.cnlapp.data.remote.repository.NotificationRepository
import com.barikoi.cnlapp.data.remote.repository.NotificationRepositoryImpl
import com.barikoi.cnlapp.data.remote.repository.OrderRepository
import com.barikoi.cnlapp.data.remote.repository.OrderRepositoryImpl
import com.barikoi.cnlapp.data.remote.repository.ProductRepository
import com.barikoi.cnlapp.data.remote.repository.ProductRepositoryImpl
import com.barikoi.cnlapp.data.remote.repository.ProductStockRepository
import com.barikoi.cnlapp.data.remote.repository.ProductStockRepositoryImpl
import com.barikoi.cnlapp.data.remote.repository.SoRepository
import com.barikoi.cnlapp.data.remote.repository.SoRepositoryImpl
import com.barikoi.cnlapp.data.remote.repository.SocketRepository
import com.barikoi.cnlapp.data.remote.repository.SocketRepositoryImpl
import com.barikoi.cnlapp.data.remote.repository.SummaryRepository
import com.barikoi.cnlapp.data.remote.repository.SummaryRepositoryImpl
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


    @Provides
    fun providesGiftRepository(apiService: ApiService): AddGiftRepository =
        AddGiftRepositoryImpl(apiService)

    @Provides
    fun providesSummaryRepository(apiService: ApiService): SummaryRepository =
        SummaryRepositoryImpl(apiService)

    @Provides
    fun providesSORepository(apiService: ApiService): SoRepository =
        SoRepositoryImpl(apiService)


    @Provides
    fun providesProductRepository(apiService: ApiService): ProductRepository =
        ProductRepositoryImpl(apiService)

    @Provides
    fun providesOrderRepository(apiService: ApiService): OrderRepository =
        OrderRepositoryImpl(apiService)
}