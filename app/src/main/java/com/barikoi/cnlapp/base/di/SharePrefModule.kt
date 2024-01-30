package com.barikoi.cnlapp.base.di

import android.content.Context
import com.barikoi.cnlapp.utils.SharePrefUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SharePrefModule {

    @Provides
    @Singleton
    fun provideSharePref(@ApplicationContext context: Context): SharePrefUtils =
        SharePrefUtils(context)
}