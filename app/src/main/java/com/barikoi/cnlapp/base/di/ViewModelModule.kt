package com.barikoi.cnlapp.base.di

import com.barikoi.cnlapp.data.remote.ApiService
import com.barikoi.cnlapp.data.remote.RouteRepository
import com.barikoi.cnlapp.data.remote.RouteRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @Provides
    fun provides(apiService: ApiService): RouteRepository = RouteRepositoryImpl(apiService)
}