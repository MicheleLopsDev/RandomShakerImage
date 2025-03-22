package com.example.randomshakerimage

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://pixabay.com/"

    // Abilita il lenient mode su Gson
    private val gson = GsonBuilder()
        .setLenient()
        .create()

    val pixabayService: PixabayService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(PixabayService::class.java)
    }
}
