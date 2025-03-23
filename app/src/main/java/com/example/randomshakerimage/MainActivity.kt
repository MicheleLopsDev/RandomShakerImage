package com.example.randomshakerimage

import android.content.Context
import android.graphics.Matrix
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.imagesha.ShakeDetector
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.Button
import android.widget.EditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var shakeDetector: ShakeDetector
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var imageView: ImageView
    // Inserisci la tua chiave API Pixabay qui
    private val pixabayApiKey = "49473800-b0e37ff4a4576833a14090ca0"
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var scaleFactor = 1.0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var matrix = Matrix()
    private var query = "nature"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.imageView)
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        searchEditText.setText(this.query)
        // Assegna il comportamento al pulsante di ricerca
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim() // Legge il testo e rimuove spazi inutili
            if (query.isNotEmpty()) {
                Log.d("MainActivity", "Ricerca avviata per: $query")
                this.query = query
                vibrate()
                loadRandomImage() // Esegui la ricerca con la parola chiave
            } else {
                Log.w("MainActivity", "Il campo di ricerca è vuoto!")
            }
        }

        // Initialize scaleGestureDetector here
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())
        // Set the onTouchListener here, after scaleGestureDetector is initialized
        imageView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchX = event.x
                    lastTouchY = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY
                    matrix.postTranslate(dx, dy)
                    imageView.imageMatrix = matrix
                    lastTouchX = event.x
                    lastTouchY = event.y
                }
            }
            true
        }

        // Inizializza il sensore e il rilevatore di shake
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        shakeDetector = ShakeDetector(this) {
            Log.i("MainActivity", "Shake rilevato! Cambio immagine...")
            loadRandomImage()
        }
    }

    private fun loadRandomImage() {
        Log.d("MainActivity", "Cambio immagine... (implementa qui il caricamento da Bing)")
        // Esegui la ricerca in background usando una coroutine
        // Esegui la chiamata in una coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.pixabayService.searchImagesRaw(
                    key = pixabayApiKey,
                    query = this@MainActivity.query
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
                    Log.e("MainActivity", "Nessuna immagine trovata per la query: ${this@MainActivity.query}")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Errore durante la richiesta: ${e.message}", e)
            }
        }
    }

    private fun vibrate() {
        val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (vibratorService.hasVibrator()) {
            val vibrationEffect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE) // 50ms di vibrazione
            vibratorService.vibrate(vibrationEffect)
        }


    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = maxOf(0.1f, minOf(scaleFactor, 5.0f)) // Limita lo zoom tra 0.1x e 5x

            matrix.setScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
            imageView.imageMatrix = matrix
            return true
        }
    }

}
