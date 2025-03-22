package com.example.randomshakerimage

import retrofit2.http.GET
import retrofit2.http.Query

interface PixabayService {
    @GET("api/")
    suspend fun searchImagesRaw(
        @Query("key") key: String,
        @Query("q") query: String,
        //@Query("image_type") imageType: String = "photo",
        @Query("per_page") perPage: Int = 10,
        @Query("safesearch") safe: Boolean = false

    ): PixabayResponse
}
