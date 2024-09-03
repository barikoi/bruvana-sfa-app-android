package com.barikoi.cnlapp.base.di

import com.barikoi.cnlapp.data.remote.api.ApiService
import com.barikoi.cnlapp.data.remote.repository.RouteRepository
import com.barikoi.cnlapp.data.remote.repository.RouteRepositoryImpl
import com.barikoi.cnlapp.data.remote.api.SocketApiService
import com.barikoi.cnlapp.data.remote.repository.ProductStockRepository
import com.barikoi.cnlapp.data.remote.repository.ProductStockRepositoryImpl
import com.barikoi.cnlapp.data.remote.repository.SocketRepository
import com.barikoi.cnlapp.data.remote.repository.SocketRepositoryImpl
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
    fun providesSocketRepository(socketApiService: SocketApiService): SocketRepository =
        SocketRepositoryImpl(socketApiService)

    @Provides
    fun providesProductStockRepository(apiService: ApiService): ProductStockRepository =
        ProductStockRepositoryImpl(apiService)
}