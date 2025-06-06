package com.sss.mysimilarteenieping.di

import com.google.gson.Gson
import com.sss.mysimilarteenieping.BuildConfig // For API Key (needs to be defined in build.gradle)
import com.sss.mysimilarteenieping.data.remote.ChatGptApiService
import com.sss.mysimilarteenieping.data.remote.NaverShoppingApiService
import com.sss.mysimilarteenieping.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Qualifier annotations for different OkHttpClient and Retrofit instances
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ChatGptOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NaverShoppingOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ChatGptRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NaverShoppingRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
    }

    @Singleton
    @Provides
    @ChatGptOkHttpClient
    fun provideChatGptOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${BuildConfig.CHAT_GPT_API_KEY}")
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Singleton
    @Provides
    @NaverShoppingOkHttpClient
    fun provideNaverShoppingOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()
                    .addHeader("X-Naver-Client-Id", BuildConfig.NAVER_CLIENT_ID)
                    .addHeader("X-Naver-Client-Secret", BuildConfig.NAVER_CLIENT_SECRET)
                    .addHeader("User-Agent", "MySimilarTeenieping-Android/1.0")
                
                val request = requestBuilder.build()
                
                // 디버그 로깅
                if (BuildConfig.DEBUG) {
                    android.util.Log.d("NetworkModule", "Request URL: ${request.url}")
                    android.util.Log.d("NetworkModule", "Request Headers: ${request.headers}")
                }
                
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Singleton
    @Provides
    @ChatGptRetrofit
    fun provideChatGptRetrofit(@ChatGptOkHttpClient chatGptOkHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.CHAT_GPT_BASE_URL)
            .client(chatGptOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Singleton
    @Provides
    @NaverShoppingRetrofit
    fun provideNaverShoppingRetrofit(@NaverShoppingOkHttpClient naverShoppingOkHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.NAVER_SHOPPING_BASE_URL)
            .client(naverShoppingOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Singleton
    @Provides
    fun provideChatGptApiService(@ChatGptRetrofit retrofit: Retrofit): ChatGptApiService {
        return retrofit.create(ChatGptApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideNaverShoppingApiService(@NaverShoppingRetrofit retrofit: Retrofit): NaverShoppingApiService {
        return retrofit.create(NaverShoppingApiService::class.java)
    }

} 