package com.example.randomshakerimage

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service interface for interacting with MyMemory API
 * Provides methods to search and retrieve images
 */
interface MyMemoryService {
    /**
     * Translate text using MyMemory  API
     * @param query Search term to translate
     * @param langpair language for translation (default it|eng)
     * @return PixabayResponse containing list of images
     */
    @GET("get")
    suspend fun getTranslation(
        @Query("q") query: String,
        @Query("langpair") languagePair: String = "it|en" // Example default
    ): MyMemoryResponse

}