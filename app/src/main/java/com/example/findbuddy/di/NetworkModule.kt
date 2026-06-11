package com.example.findbuddy.di

import com.example.findbuddy.data.api.AuthApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: com.example.findbuddy.data.api.AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(
        retrofit: Retrofit
    ): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAccountApi(
        retrofit: Retrofit
    ): com.example.findbuddy.data.api.AccountApi {
        return retrofit.create(com.example.findbuddy.data.api.AccountApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTransactionApi(
        retrofit: Retrofit
    ): com.example.findbuddy.data.api.TransactionApi {
        return retrofit.create(com.example.findbuddy.data.api.TransactionApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(
        @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context
    ): android.content.SharedPreferences {
        return context.getSharedPreferences("finbuddy_prefs", android.content.Context.MODE_PRIVATE)
    }
}
