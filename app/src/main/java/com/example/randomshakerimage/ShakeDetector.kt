package com.example.imagesha

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log

/**
 * Detects device shake using accelerometer sensor
 * @param context Application context
 * @param onShake Callback function triggered when a shake is detected
 */
class ShakeDetector(
    private val context: Context,
    private val onShake: () -> Unit
) : SensorEventListener {

    // Timestamp of last shake to prevent multiple rapid shakes
    private var shakeTimestamp: Long = 0

    // Acceleration threshold to trigger a shake event
    private val shakeThreshold = 7.0f

    // Minimum time between two shake events
    private val shakeSlopTimeMs = 1000

    /**
     * Detect shake based on accelerometer sensor values
     * @param event Sensor event data
     */
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        // Normalize acceleration values
        val (x, y, z) = event.values
        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        // Calculate total acceleration force
        val gForce = kotlin.math.sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

        // Check if acceleration exceeds shake threshold
        if (gForce > shakeThreshold) {
            Log.d("ShakeDetector", "Total acceleration: $gForce")

            val now = System.currentTimeMillis()
            // Prevent multiple rapid shake events
            if (shakeTimestamp + shakeSlopTimeMs > now) return

            shakeTimestamp = now
            vibrate()
            onShake()
        }
    }

    /**
     * Vibrate the device when a shake is detected
     */
    private fun vibrate() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            val vibrationEffect = VibrationEffect.createOneShot(
                500,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
            vibrator.vibrate(vibrationEffect)
            Log.d("ShakeDetector", "Vibration activated for 500ms")
        } else {
            Log.w("ShakeDetector", "Device does not support vibration")
        }
    }

    /**
     * Handle sensor accuracy changes (currently unused)
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}