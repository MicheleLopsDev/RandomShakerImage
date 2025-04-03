package com.example.randomshakerimage

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.WindowInsetsController
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.random.Random

/**
 * MainActivity for the Random Shaker Image app
 * Allows users to search and display random images with shake and zoom interactions
 * Loads Pixabay API key from properties file
 */
class MainActivity : AppCompatActivity() {

    // Crea un'istanza dell'API di MyMemory utilizzando il client Retrofit creato in RetrofitClient
    private var myMemoryApi = MyMemoryClient.create()

    // UI components
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var imageView: ImageView
    private lateinit var radioGroup: RadioGroup
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    // GestureDetector, creato tramite una funzione dedicata
    private lateinit var gestureDetector: GestureDetector


    // Search query management
    private var query = "house"

    // Pixabay API key
    private var pixabayApiKey: String? = null
    private var currentScaleTypeIndex = 0

    // Array con i diversi ScaleType che vogliamo ciclare
    private val scaleTypes = arrayOf(
        ImageView.ScaleType.CENTER_CROP,
        ImageView.ScaleType.FIT_CENTER,
        ImageView.ScaleType.CENTER_INSIDE,
        ImageView.ScaleType.FIT_XY,
        ImageView.ScaleType.MATRIX,
        ImageView.ScaleType.CENTER,
        ImageView.ScaleType.FIT_START,
        ImageView.ScaleType.FIT_END
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edit toolbar colour
        setupToolBar()

        // Load API key from properties
        loadApiKey()

        // Initialize UI components
        initializeUIComponents()

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

        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)

        imageView = findViewById(R.id.imageView)
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.imageButton)
        searchEditText.setText(query)
        radioGroup = findViewById(R.id.languageRadioGroup)

        navigationView = findViewById(R.id.navigation_view)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.scale_center_crop -> imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                R.id.scale_fit_center -> imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                R.id.scale_fit_xy -> imageView.scaleType = ImageView.ScaleType.FIT_XY
                R.id.scale_center_inside -> imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                R.id.scale_center -> imageView.scaleType = ImageView.ScaleType.CENTER
                R.id.scale_matrix -> imageView.scaleType = ImageView.ScaleType.MATRIX
                R.id.scale_fit_start -> imageView.scaleType = ImageView.ScaleType.FIT_START
                R.id.scale_fit_end -> imageView.scaleType = ImageView.ScaleType.FIT_END
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        imageView.scaleType = scaleTypes[currentScaleTypeIndex]

        // Crea il GestureDetector usando la funzione separata
        gestureDetector = createGestureDetector()


        // Imposta l'OnTouchListener sull'ImageView per usare il GestureDetector
        imageView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        searchEditText.setOnEditorActionListener(
            { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    action()
                    return@setOnEditorActionListener true
                }
                false
            }
        )

        // Configure search button behavior
        searchButton.setOnClickListener {
            action()
        }
    }

    private fun action() {
        val searchQuery = searchEditText.text.toString().trim().replace(' ', '+')
        if (searchQuery.isNotEmpty()) {
            Log.d("MainActivity", "Search started for: $searchQuery")
            query = searchQuery
            vibrate()
            searchText()

        } else {
            Log.w("MainActivity", "Search field is empty!")
            Toast.makeText(
                this,
                "Please enter a search term",
                Toast.LENGTH_SHORT
            ).show()
        }
    }



    private fun changeScaleType() {
        currentScaleTypeIndex = (currentScaleTypeIndex + 1) % scaleTypes.size
        imageView.scaleType = scaleTypes[currentScaleTypeIndex]
        Toast.makeText(this, "ScaleType: ${scaleTypes[currentScaleTypeIndex]}", Toast.LENGTH_SHORT).show()
    }

    private fun setWallpaperWithScaleType() {
        val drawable = imageView.drawable as? BitmapDrawable
        val originalBitmap = drawable?.bitmap ?: return

        val wallpaperManager = WallpaperManager.getInstance(applicationContext)
        val screenWidth = wallpaperManager.desiredMinimumWidth
        try {
            val screenHeight = wallpaperManager.desiredMinimumHeight

            val transformedBitmap = transformBitmapToScaleType(originalBitmap, screenWidth, screenHeight, imageView.scaleType)

            wallpaperManager.setBitmap(transformedBitmap)
            Toast.makeText(this, "Sfondo impostato con ${imageView.scaleType}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Errore nell'impostazione dello sfondo", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun transformBitmapToScaleType(bitmap: Bitmap, width: Int, height: Int, scaleType: ImageView.ScaleType): Bitmap {
        val matrix = Matrix()
        val scaleX = width.toFloat() / bitmap.width
        val scaleY = height.toFloat() / bitmap.height

        when (scaleType) {
            ImageView.ScaleType.FIT_CENTER -> {
                val scale = minOf(scaleX, scaleY)
                matrix.postScale(scale, scale)
                val newX = (width - bitmap.width * scale) / 2
                val newY = (height - bitmap.height * scale) / 2
                matrix.postTranslate(newX, newY)
            }
            ImageView.ScaleType.CENTER_CROP -> {
                val scale = maxOf(scaleX, scaleY)
                matrix.postScale(scale, scale)
                val newX = (width - bitmap.width * scale) / 2
                val newY = (height - bitmap.height * scale) / 2
                matrix.postTranslate(newX, newY)
            }
            ImageView.ScaleType.FIT_XY -> {
                matrix.postScale(scaleX, scaleY)
            }
            ImageView.ScaleType.CENTER_INSIDE -> {
                val scale = minOf(scaleX, scaleY, 1f)
                matrix.postScale(scale, scale)
                val newX = (width - bitmap.width * scale) / 2
                val newY = (height - bitmap.height * scale) / 2
                matrix.postTranslate(newX, newY)
            }
            ImageView.ScaleType.CENTER -> {
                val newX = (width - bitmap.width) / 2f
                val newY = (height - bitmap.height) / 2f
                matrix.postTranslate(newX, newY)
            }
            ImageView.ScaleType.MATRIX -> {
                // Esempio: ruota di 45 gradi e scala al 75%
                matrix.postScale(0.75f, 0.75f)
                matrix.postRotate(45f, bitmap.width / 2f, bitmap.height / 2f)
                matrix.postTranslate((width - bitmap.width) / 2f, (height - bitmap.height) / 2f)
            }
            ImageView.ScaleType.FIT_START -> {
                val scale = minOf(scaleX, scaleY)
                matrix.postScale(scale, scale)
            }
            ImageView.ScaleType.FIT_END -> {
                val scale = minOf(scaleX, scaleY)
                matrix.postScale(scale, scale)
                val translateY = height - (bitmap.height * scale)
                matrix.postTranslate(0f, translateY)
            }
            else -> return bitmap
        }

        val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)
        canvas.drawColor(Color.BLACK) // Sfondo nero per le aree vuote
        canvas.drawBitmap(bitmap, matrix, null)

        return outputBitmap
    }

    fun removeHtmlTags(input: String): String {
        // Usa una regex per rimuovere i tag HTML/XML
        return input.replace(Regex("<.*?>"), "")
    }


    private fun searchText() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val translationResponse: MyMemoryResponse = withContext(Dispatchers.IO) {
                    myMemoryApi.getTranslation(query)  // Chiamata al metodo nell'interfaccia MyMemoryService
                }
                if (radioGroup.checkedRadioButtonId == R.id.italianoRadioButton) {
                    if (translationResponse.responseStatus == 200) {
                        val translatedText = translationResponse.responseData.translatedText
                        if (translatedText != query) {
                            query = removeHtmlTags(translatedText).trim()
                                .lowercase(Locale.getDefault())
                            searchEditText = findViewById(R.id.searchEditText)
                            searchEditText.setText(query)
                        }
                        radioGroup.check(R.id.englishRadioButton)
                        loadRandomImage()
                        Log.d("MainActivity", "Translated Text: $translatedText")
                    } else {
                        Log.e(
                            "MainActivity",
                            "Translation error: ${translationResponse.responseStatus}"
                        )
                    }
                } else {
                    loadRandomImage()
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
     * Crea e restituisce un GestureDetector configurato per cambiare lo ScaleType dell'ImageView.
     */
    private fun createGestureDetector(): GestureDetector {
        return GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                action() // ricerca una immagine al click
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                setWallpaperWithScaleType() // Imposta lo sfondo con un doppio tap
                return true
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("query", query) // Salva la stringa di ricerca
        outState.putInt("scaleTypeIndex", currentScaleTypeIndex) // Salva lo scaleType
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        query = savedInstanceState.getString("query", "house") // Recupera la query salvata
        currentScaleTypeIndex = savedInstanceState.getInt("scaleTypeIndex", 0) // Recupera lo ScaleType
        imageView.scaleType = scaleTypes[currentScaleTypeIndex] // Applica lo ScaleType
        searchEditText.setText(query) // Aggiorna il campo di ricerca
    }


}