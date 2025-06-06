package com.sss.mysimilarteenieping.di

import android.content.Context
import android.content.res.AssetManager
import com.google.gson.Gson
import com.sss.mysimilarteenieping.data.remote.FirebaseService
import com.sss.mysimilarteenieping.data.remote.ChatGptApiService
import com.sss.mysimilarteenieping.data.repository.ShoppingRepository
import com.sss.mysimilarteenieping.data.repository.ShoppingRepositoryImpl
import com.sss.mysimilarteenieping.data.repository.HistoryRepository
import com.sss.mysimilarteenieping.data.repository.HistoryRepositoryImpl
import com.sss.mysimilarteenieping.data.repository.TeeniepingRepository
import com.sss.mysimilarteenieping.data.repository.TeeniepingRepositoryImpl
import com.sss.mysimilarteenieping.data.repository.ChatGptRepository
import com.sss.mysimilarteenieping.data.repository.ChatGptRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideHistoryRepository(firebaseService: FirebaseService): HistoryRepository {
        return HistoryRepositoryImpl(firebaseService)
    }

    @Singleton
    @Provides
    fun provideTeeniepingRepository(
        firebaseService: FirebaseService,
        @ApplicationContext context: Context,
        gson: Gson
    ): TeeniepingRepository {
        val assetManager: AssetManager = context.assets
        return TeeniepingRepositoryImpl(firebaseService, assetManager, gson)
    }

    @Singleton
    @Provides
    fun provideShoppingRepository(
        // TODO: 추후 NaverShoppingApiService 주입 (NetworkModule에서 제공)
    ): ShoppingRepository {
        return ShoppingRepositoryImpl(/* navverShoppingApiService */)
    }

    @Singleton
    @Provides
    fun provideChatGptRepository(chatGptApiService: ChatGptApiService): ChatGptRepository {
        return ChatGptRepositoryImpl(chatGptApiService)
    }
}