package com.example.randomshakerimage

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.imagesha.BingImageScraper
import com.example.imagesha.ShakeDetector
import android.util.Log
import android.view.MotionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var shakeDetector: ShakeDetector
    private lateinit var imageView: ImageView
    // Inserisci la tua chiave API Pixabay qui
    private val pixabayApiKey = "49473800-b0e37ff4a4576833a14090ca0"
    // Query di esempio, puoi modificarla o renderla dinamica
    private val query = "nature"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.imageView)

        // Inizializza il sensore e il rilevatore di shake
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        shakeDetector = ShakeDetector(this) {
            Log.i("MainActivity", "Shake rilevato! Cambio immagine...")
            cambiaImmagine()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            Log.d("MainActivity", "Schermo toccato! Caricamento nuova immagine...")
            vibrate()
            cambiaImmagine()
        }
        return super.onTouchEvent(event)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(shakeDetector)
    }

    private fun cambiaImmagine() {
        Log.d("MainActivity", "Cambio immagine... (implementa qui il caricamento da Bing)")
        // Esegui la ricerca in background usando una coroutine
        // Esegui la chiamata in una coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.pixabayService.searchImagesRaw(
                    key = pixabayApiKey,
                    query = query
                )
                val images = response.hits
                if (images.isNotEmpty()) {
                    // Scegli un'immagine a caso dalla lista
                    val randomImage = images[Random.nextInt(images.size)]
                    val imageUrl = randomImage.largeImageURL  // oppure webformatURL

                    // Aggiorna l'UI nel thread principale
                    withContext(Dispatchers.Main) {
                        Glide.with(this@MainActivity)
                            .load(imageUrl)
                            .into(imageView)
                    }
                } else {
                    Log.e("MainActivity", "Nessuna immagine trovata per la query: $query")
                }
//                if (response.isSuccessful) {
//                    // Leggi la response in una variabile String
//                    val rawResponse: String = response.body()?.string() ?: ""
//                    // Ricorda: una volta consumato, il ResponseBody non è riutilizzabile!
//                    withContext(Dispatchers.Main) {
//                        Log.d("MainActivity", "Raw Response: $rawResponse")
//                        // Puoi usare rawResponse come preferisci
//                    }
//                } else {
//                    Log.e("MainActivity", "Errore nella chiamata: ${response.errorBody()?.string()}")
//                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Errore durante la richiesta: ${e.message}", e)
            }
        }
    }

    private fun vibrate() {
        val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) { // Controlla se il dispositivo supporta la vibrazione
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(500) // Per versioni più vecchie
            }
            Log.d("ShakeDetector", "Vibrazione attivata per 500ms")
        } else {
            Log.w("ShakeDetector", "Il dispositivo non supporta la vibrazione")
        }
    }

}
