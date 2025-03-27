package com.example.randomshakerimage

import android.graphics.Matrix
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.WindowInsetsController
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.imagesha.ShakeDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * MainActivity for the Random Shaker Image app
 * Allows users to search and display random images with shake and zoom interactions
 * Loads Pixabay API key from properties file
 */
class MainActivity : AppCompatActivity() {

    // Sensor and shake detection components
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var shakeDetector: ShakeDetector

    // Crea un'istanza dell'API di MyMemory utilizzando il client Retrofit creato in RetrofitClient
    private var myMemoryApi = MyMemoryClient.create()

    // UI components
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var imageView: ImageView

    // Image scaling and touch interaction
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var scaleFactor = 1.0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var matrix = Matrix()

    // Search query management
    private var query = "casa"

    // Pixabay API key
    private var pixabayApiKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edit toolbar colour
        setupToolBar()

        // Load API key from properties
        loadApiKey()

        // Initialize UI components
        initializeUIComponents()

        //default Image on create
        loadRandomImage()

        // Setup sensor and shake detection
        setupShakeDetection()

        // Configure image scaling and touch interactions
        setupImageInteractions()
    }

    private fun setupToolBar() {
        setContentView(R.layout.activity_main)

        // Check if we're running on Android 10 (API level 29) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.statusBarColor = ContextCompat.getColor(this, android.R.color.black)
            //make sure you can see the icon in the status bar
            window.decorView.windowInsetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        }
    }

    /**
     * Load Pixabay API key from properties file
     */
    private fun loadApiKey() {
        pixabayApiKey = ApiKeyConfig.loadApiKey(
            context = this,
            filename = "api_keys.properties",
            key = "PIXABAY_API_KEY"
        )

        // Optionally show a toast if API key is not found
        if (pixabayApiKey == null) {
            Toast.makeText(
                this,
                "Warning: Pixabay API key not found",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Initialize UI components and set initial query
     */
    private fun initializeUIComponents() {
        imageView = findViewById(R.id.imageView)
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.imageButton)
        searchEditText.setText(query)


        searchEditText.setOnEditorActionListener(
            { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val searchQuery = searchEditText.text.toString().trim().replace(' ', '+')
                    if (searchQuery.isNotEmpty()) {
                        Log.d("MainActivity", "Search started for: $searchQuery")
                        query = searchQuery
                        vibrate()
                        searchText()
                        loadRandomImage()
                    } else {
                        Log.w("MainActivity", "Search field is empty!")
                        Toast.makeText(
                            this,
                            "Please enter a search term",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@setOnEditorActionListener true
                }
                false
            }
        )

        // Configure search button behavior
        searchButton.setOnClickListener {
            val searchQuery = searchEditText.text.toString().trim().replace(' ', '+')
            if (searchQuery.isNotEmpty()) {
                Log.d("MainActivity", "Search started for: $searchQuery")
                query = searchQuery
                vibrate()
                searchText()
                loadRandomImage()
            } else {
                Log.w("MainActivity", "Search field is empty!")
                Toast.makeText(
                    this,
                    "Please enter a search term",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Setup shake detection using accelerometer
     */
    private fun setupShakeDetection() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        shakeDetector = ShakeDetector(this) {
            Log.i("MainActivity", "Shake detected! Changing image...")
            loadRandomImage()
        }
    }


    /**
     * Configure image scaling and touch interactions
     */
    private fun setupImageInteractions() {
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())
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
    }


    private fun searchText() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val translationResponse: MyMemoryResponse = withContext(Dispatchers.IO) {
                    myMemoryApi.getTranslation(query)  // Chiamata al metodo nell'interfaccia MyMemoryService
                }

                // Resto del codice invariato
                if (translationResponse.responseStatus == 200) {
                    val translatedText = translationResponse.responseData.translatedText
                    if (translatedText != query) {
                        query = translatedText
                    }
                    Log.d("MainActivity", "Translated Text: $translatedText")
                } else {
                    Log.e(
                        "MainActivity",
                        "Translation error: ${translationResponse.responseStatus}"
                    )
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error: ${e.message}")
            }
        }
    }

    /**
     * Load a random image from Pixabay based on current query
     */
    private fun loadRandomImage() {
        // Check if API key is available
        val apiKey = pixabayApiKey
        if (apiKey == null) {
            Toast.makeText(
                this,
                "Cannot load image: API key missing",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        Log.d("MainActivity", "Changing image...")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = PixaBayClient.pixabayService.searchImagesRaw(
                    key = apiKey,
                    query = query
                )
                val images = response.hits
                if (images.isNotEmpty()) {
                    val randomImage = images[Random.nextInt(images.size)]
                    val imageUrl = randomImage.largeImageURL

                    // Update UI on main thread
                    withContext(Dispatchers.Main) {
                        Glide.with(this@MainActivity)
                            .load(imageUrl)
                            .into(imageView)
                    }
                } else {
                    Log.e("MainActivity", "No images found for query: $query")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "No images found for '$query'",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error during request: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error loading image: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /**
     * Trigger device vibration when button or shake occurs
     */
    private fun vibrate() {
        val vibratorService = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (vibratorService.hasVibrator()) {
            val vibrationEffect =
                VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
            vibratorService.vibrate(vibrationEffect)
        }
    }

    /**
     * Inner class to handle image scaling gestures
     */
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = maxOf(0.1f, minOf(scaleFactor, 5.0f)) // Limit zoom between 0.1x and 5x

            matrix.setScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
            imageView.imageMatrix = matrix
            return true
        }
    }

    /**
     * Lifecycle method to register shake detector
     */
    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(
                shakeDetector,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    /**
     * Lifecycle method to unregister shake detector
     */
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(shakeDetector)
    }
}