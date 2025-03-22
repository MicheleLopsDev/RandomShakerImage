package com.example.imagesha

import android.util.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlin.random.Random


object BingImageScraper {
    fun fetchBingImage(query: String): String? {
        return try {
            // Costruisci l'URL per la ricerca immagini su Bing
            //val searchUrl = "https://www.bing.com/images/search?q=${query.replace(" ", "+")}"
            val searchUrl = "https://www.bing.com/images/search?q=sexy%20china%20female&adlt=off&qs=n&form=QBIR&sp=-1&lq=0&pq=sexy%20china%20female&sc=0-17&cvid=906172E6A63A486D8B08B0BC0D90CDD9&ghsh=0&ghacc=0&first=1"
            val doc: Document = Jsoup.connect(searchUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "Chrome/91.0.4472.124 Safari/537.36")
                .cookie("SRCHHP", "ADLT=OFF")
                .timeout(50000)
                .get()

            // Seleziona i tag <img> dalla pagina HTML
            val html = doc.html()
            Log.d("ShakeDetector","html: " + html)

            val images = doc.select(".mimg")
            if (images.size > 1) {
                // Salta il primo risultato, che spesso non è utile
                val randomIndex = Random.nextInt(1, images.size)
                val src = images[randomIndex].attr("data-src").ifEmpty { images[randomIndex].attr("src") }
                if (src.isNotEmpty()) src else null
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
