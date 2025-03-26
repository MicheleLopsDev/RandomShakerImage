package com.example.randomshakerimage

/**
 * Data class representing the complete Pixabay API response
 * @property total Total number of images found
 * @property totalHits Number of images returned in this response
 * @property hits List of image details
 */
data class PixabayResponse(
    val total: Int,
    val totalHits: Int,
    val hits: List<PixabayImage>
)

/**
 * Data class representing a single image from Pixabay
 * Contains comprehensive metadata about the image
 */
data class PixabayImage(
    val id: Int,               // Unique identifier for the image
    val pageURL: String,       // URL to the Pixabay page
    val type: String,          // Type of image (photo, illustration, etc.)
    val tags: String,          // Comma-separated tags
    val previewURL: String,    // Small preview image URL
    val previewWidth: Int,     // Preview image width
    val previewHeight: Int,    // Preview image height
    val webformatURL: String,  // Standard web format image URL
    val webformatWidth: Int,   // Web format image width
    val webformatHeight: Int,  // Web format image height
    val largeImageURL: String, // Large, high-resolution image URL
    val imageWidth: Int,       // Original image width
    val imageHeight: Int,      // Original image height
    val imageSize: Int,        // Size of the image in bytes
    val views: Int,            // Number of views
    val downloads: Int,        // Number of downloads
    val collections: Int,      // Number of collections
    val likes: Int,            // Number of likes
    val comments: Int,         // Number of comments
    val user_id: Int,          // User ID of the uploader
    val user: String,          // Username of the uploader
    val userImageURL: String   // URL of the user's profile image
)