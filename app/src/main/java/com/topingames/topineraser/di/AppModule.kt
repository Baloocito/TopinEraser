package com.topingames.topineraser.di

import com.topingames.topineraser.data.repository.BackgroundRemovalRepository
import com.topingames.topineraser.data.repository.BackgroundRemovalRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindBackgroundRemovalRepository(
        impl: BackgroundRemovalRepositoryImpl
    ): BackgroundRemovalRepository
}
