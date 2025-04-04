package com.example.randomshakerimage

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service interface for interacting with Pixabay API
 * Provides methods to search and retrieve images
 */
interface PixabayService {
    /**
     * Search for images using Pixabay API
     * @param key Pixabay API key for authentication
     * @param query Search term for images
     * @param perPage Maximum number of images to return (default 1000)
     * @param safe Enable safe search filter (default false)
     * @return PixabayResponse containing list of images
     */
    @GET("api/")
    suspend fun searchImagesRaw(
        @Query("key") key: String,
        @Query("q") query: String,
        @Query("per_page") perPage: Int = 200,
        @Query("safesearch") safe: Boolean = false
    ): PixabayResponse
}