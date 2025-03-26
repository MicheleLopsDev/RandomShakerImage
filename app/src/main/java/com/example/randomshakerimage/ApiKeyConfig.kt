package com.example.randomshakerimage

import android.content.Context
import android.util.Log
import java.util.Properties

/**
 * Utility object to load API keys from properties file
 */
object ApiKeyConfig {
    /**
     * Load API key from properties file
     * @param context Application context
     * @param filename Name of the properties file
     * @param key Key to retrieve from properties
     * @return API key or null if not found
     */
    fun loadApiKey(context: Context, filename: String, key: String): String? {
        return try {
            context.assets.open(filename).use { inputStream ->
                val properties = Properties()
                properties.load(inputStream)
                properties.getProperty(key)
            }
        } catch (e: Exception) {
            // Log the error for debugging
            Log.e("ApiKeyConfig", "Error loading API key: ${e.message}",e)
            null
        }
    }
}