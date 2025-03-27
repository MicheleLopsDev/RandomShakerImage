package com.example.imagesha

import android.util.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlin.random.Random

/**
 * Utility object for scraping image search results from Bing
 * Note: Web scraping may violate Bing's terms of service and can be unreliable
 */
object BingImageScraper {
    // Configurable timeout and user agent for web requests
    private const val REQUEST_TIMEOUT_MS = 50000
    private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/91.0.4472.124 Safari/537.36"

    /**
     * Fetch a random image URL from Bing image search
     * @param query Search term for images
     * @return Image URL or null if no images found or error occurs
     */
    fun fetchBingImage(query: String): String? {
        return try {
            // Construct Bing image search URL with the query
            val searchUrl = buildSearchUrl(query)

            // Connect to Bing and fetch search results
            val doc = connectToBing(searchUrl)

            // Extract and validate image URLs
            extractRandomImageUrl(doc)
        } catch (e: Exception) {
            Log.e("BingImageScraper", "Error fetching image: ${e.message}")
            null
        }
    }

    /**
     * Build URL for Bing image search
     * @param query Search term for images
     * @return Encoded search URL
     */
    private fun buildSearchUrl(query: String): String {
        // URL encode the query and remove potentially problematic characters
        val encodedQuery = query.replace(" ", "+")
            .replace("[^a-zA-Z0-9+]".toRegex(), "")

        return "https://www.bing.com/images/search?q=$encodedQuery&adlt=off&qs=n&form=QBIR"
    }

    /**
     * Establish connection to Bing image search
     * @param searchUrl Constructed search URL
     * @return Parsed HTML document
     */
    private fun connectToBing(searchUrl: String): Document {
        return Jsoup.connect(searchUrl)
            .userAgent(USER_AGENT)
            .cookie("SRCHHP", "ADLT=OFF")
            .timeout(REQUEST_TIMEOUT_MS)
            .get()
    }

    /**
     * Extract a random image URL from search results
     * @param doc Parsed HTML document
     * @return Image URL or null
     */
    private fun extractRandomImageUrl(doc: Document): String? {
        // Select image elements
        val images = doc.select(".mimg")

        return when {
            images.size > 1 -> {
                // Skip first result, often not a useful image
                val randomIndex = Random.nextInt(1, images.size)
                val image = images[randomIndex]

                // Prefer data-src, fallback to src
                val src = image.attr("data-src").ifEmpty {
                    image.attr("src")
                }

                // Return non-empty URL
                src.takeIf { it.isNotEmpty() }
            }

            else -> null
        }
    }
}