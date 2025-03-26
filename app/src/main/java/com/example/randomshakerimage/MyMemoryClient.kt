package com.example.randomshakerimage

import com.example.randomshakerimage.PixaBayClient.okHttpClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton object to create and manage Retrofit client for MyMemory API
 */
object MyMemoryClient {
    // Base URL for MyMemory API
    private const val BASE_URL = "https://api.mymemory.translated.net/"

    // Example Interceptor for OkHttp
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Log request/response body
    }

    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val instance: MyMemoryService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient) // Add the OkHttpClient to Retrofit
            .build()
            .create(MyMemoryService::class.java)
    }
}