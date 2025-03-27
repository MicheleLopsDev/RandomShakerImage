package com.example.randomshakerimage

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton object to create and manage Retrofit client for Pixabay API
 */
object PixaBayClient {
    // Base URL for Pixabay API
    private const val BASE_URL = "https://pixabay.com/"

    // Example Interceptor for OkHttp
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Log request/response body
    }

    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // Gson builder with lenient parsing
    private val gson = GsonBuilder()
        .setLenient()
        .create()


    // Lazy initialization of Pixabay service
    val pixabayService: PixabayService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Add the OkHttpClient to Retrofit
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(PixabayService::class.java)
    }
}