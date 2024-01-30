package com.barikoi.cnlapp.base.di

import android.content.Context
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.base.api.NetworkFailureMessageImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ActivityModule {
    @Provides
    fun provideNetworkFailureMessage(@ApplicationContext context: Context): NetworkFailureMessage {
        return NetworkFailureMessageImpl(context)
    }
}