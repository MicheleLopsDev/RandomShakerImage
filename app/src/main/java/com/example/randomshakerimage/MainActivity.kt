package com.example.randomshakerimage

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
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
import androidx.core.graphics.createBitmap

class MainActivity : AppCompatActivity() {

    private var myMemoryApi = MyMemoryClient.create()

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var imageView: ImageView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var gestureDetector: GestureDetector

    private var query = "lingerie"
    private var pixabayApiKey: String? = null
    private var currentScaleTypeIndex = 0

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
        setupToolBar()
        loadApiKey()
        initializeUIComponents()
    }

    private fun setupToolBar() {
        setContentView(R.layout.activity_main)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.black)
        window.decorView.windowInsetsController?.setSystemBarsAppearance(
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        )
    }

    private fun loadApiKey() {
        pixabayApiKey = ApiKeyConfig.loadApiKey(
            context = this,
            filename = "api_keys.properties",
            key = "PIXABAY_API_KEY"
        )

        if (pixabayApiKey == null) {
            Toast.makeText(
                this,
                getString(R.string.toast_pixabay_key_missing),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun initializeUIComponents() {
        drawerLayout = findViewById(R.id.drawer_layout)
        imageView = findViewById(R.id.imageView)
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.imageButton)
        searchEditText.setText(query)
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
        currentScaleTypeIndex = scaleTypes.indexOf(imageView.scaleType)
        gestureDetector = createGestureDetector()
        imageView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                action()
                return@setOnEditorActionListener true
            }
            false
        }

        searchButton.setOnClickListener {
            vibrate()
            translate()
        }
    }

    private fun action() {
        val searchQuery = searchEditText.text.toString().trim().replace(' ', '+')
        if (searchQuery.isNotEmpty()) {
            Log.d("MainActivity", "Search started for: $searchQuery")
            query = searchQuery
            vibrate()
            loadRandomImage()
        } else {
            Log.w("MainActivity", "Search field is empty!")
            Toast.makeText(
                this,
                getString(R.string.toast_enter_search_term),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setWallpaperWithScaleType() {
        val drawable = imageView.drawable as? BitmapDrawable
        val originalBitmap = drawable?.bitmap ?: return

        val wallpaperManager = WallpaperManager.getInstance(applicationContext)
        val screenWidth = wallpaperManager.desiredMinimumWidth
        try {
            val screenHeight = wallpaperManager.desiredMinimumHeight
            val transformedBitmap = transformBitmapToScaleType(
                originalBitmap,
                screenWidth,
                screenHeight,
                imageView.scaleType
            )
            wallpaperManager.setBitmap(transformedBitmap)
            Toast.makeText(
                this,
                getString(R.string.toast_wallpaper_set, imageView.scaleType),
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                getString(R.string.toast_wallpaper_error),
                Toast.LENGTH_SHORT
            ).show()
            e.printStackTrace()
        }
    }

    private fun transformBitmapToScaleType(
        bitmap: Bitmap,
        width: Int,
        height: Int,
        scaleType: ImageView.ScaleType
    ): Bitmap {
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
            ImageView.ScaleType.FIT_XY -> matrix.postScale(scaleX, scaleY)
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

        val outputBitmap = createBitmap(width, height)
        val canvas = Canvas(outputBitmap)
        canvas.drawColor(Color.BLACK)
        canvas.drawBitmap(bitmap, matrix, null)
        return outputBitmap
    }

    private fun removeHtmlTags(input: String): String {
        return input.replace(Regex("<.*?>"), "")
    }

    private fun translate() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                query = searchEditText.text.toString().trim().replace(' ', '+')

                if (query.isNotEmpty()) {
                    val translationResponse: MyMemoryResponse = withContext(Dispatchers.IO) {
                        myMemoryApi.getTranslation(query)
                    }
                    if (translationResponse.responseStatus == 200) {
                        val translatedText = translationResponse.responseData.translatedText
                        if (translatedText != query) {
                            query = removeHtmlTags(translatedText).trim()
                                .lowercase(Locale.getDefault())
                            searchEditText.setText(query.replace('+', ' '))
                        }
                        Log.d("MainActivity", "Translated Text: $translatedText")
                        loadRandomImage()
                    } else {
                        Log.e(
                            "MainActivity",
                            "Translation error: ${translationResponse.responseStatus}"
                        )
                    }
                } else {
                    Log.w("MainActivity", "Search field is empty!")
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.toast_enter_search_term),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error: ${e.message}")
            }
        }
    }

    private fun loadRandomImage() {
        val apiKey = pixabayApiKey
        if (apiKey == null) {
            Toast.makeText(
                this,
                getString(R.string.toast_cannot_load_image),
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

                    withContext(Dispatchers.Main) {
                        Glide.with(this@MainActivity)
                            .load(imageUrl)
                            .placeholder(R.drawable.place_holder)
                            .error(R.drawable.error)
                            .into(imageView)
                    }
                } else {
                    Log.e("MainActivity", "No images found for query: $query")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.toast_no_images_found, query),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error during request: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.toast_error_loading_image, e.message ?: "Unknown error"),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun vibrate() {
        val vibratorService = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (vibratorService.hasVibrator()) {
            val vibrationEffect =
                VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
            vibratorService.vibrate(vibrationEffect)
        }
    }

    private fun createGestureDetector(): GestureDetector {
        return GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                action()
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                setWallpaperWithScaleType()
                return true
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("query", query)
        outState.putInt("scaleTypeIndex", currentScaleTypeIndex)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        query = savedInstanceState.getString("query", "house")
        currentScaleTypeIndex = savedInstanceState.getInt("scaleTypeIndex", 0)
        imageView.scaleType = scaleTypes[currentScaleTypeIndex]
        currentScaleTypeIndex = scaleTypes.indexOf(imageView.scaleType)
        searchEditText.setText(query.replace('+', ' '))
    }
}
