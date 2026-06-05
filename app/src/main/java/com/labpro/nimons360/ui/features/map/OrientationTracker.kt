package com.labpro.nimons360.ui.features.map

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.roundToInt

class OrientationTracker(context: Context) : SensorEventListener {
    private val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private var call: ((Float) -> Unit)? = null
    private var lastTurn: Float? = null

    fun start(onTurn: (Float) -> Unit) {
        call = onTurn
        lastTurn = null
        sensor?.let {
            manager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        manager.unregisterListener(this)
        call = null
        lastTurn = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

        val matrix = FloatArray(9)
        val values = FloatArray(3)
        SensorManager.getRotationMatrixFromVector(matrix, event.values)
        SensorManager.getOrientation(matrix, values)

        val raw = Math.toDegrees(values[0].toDouble()).toFloat()
        val turn = ((raw + 360f) % 360f * 10).roundToInt() / 10f

        val last = lastTurn
        if (last == null) {
            lastTurn = turn
            call?.invoke(turn)
        } else {
            var diff = kotlin.math.abs(last - turn)
            if (diff > 180f) {
                diff = 360f - diff
            }
            if (diff >= 3f) {
                lastTurn = turn
                call?.invoke(turn)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
