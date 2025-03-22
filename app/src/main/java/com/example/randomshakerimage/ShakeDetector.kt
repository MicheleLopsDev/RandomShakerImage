package com.example.imagesha

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log


class ShakeDetector(private val context: Context, private val onShake: () -> Unit) : SensorEventListener {



//    interface OnShakeListener {
//        fun onShake()
//    }

    private var shakeTimestamp: Long = 0
    private val shakeThreshold = 7.0f      // Soglia di accelerazione
    private val shakeSlopTimeMs = 1000         // Tempo minimo tra due shake

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val (x, y, z) = event.values
        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH


        val gForce = Math.sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()


        if (gForce > shakeThreshold) {
            Log.d("ShakeDetector", "Accelerazione: totale=$gForce")
            val now = System.currentTimeMillis()
            if (shakeTimestamp + shakeSlopTimeMs > now) return
            shakeTimestamp = now
            vibrate()
            onShake()
        }
    }


//    // Metodo per far vibrare il dispositivo
    private fun vibrate() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
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

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }
}
