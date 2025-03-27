package com.example.randomshakerimage

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MyMemoryClient {
    private const val BASE_URL = "https://api.mymemory.translated.net/"

    fun create(): MyMemoryService {  // Cambiato da MyMemoryResponse a MyMemoryService
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()

        return retrofit.create(MyMemoryService::class.java)  // Usa MyMemoryService
    }
}