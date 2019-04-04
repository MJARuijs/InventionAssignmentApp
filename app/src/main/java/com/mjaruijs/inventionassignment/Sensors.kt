package com.mjaruijs.inventionassignment

import android.hardware.*
import android.hardware.SensorManager.SENSOR_DELAY_FASTEST
import java.text.DecimalFormat

class Sensors(private val sensorManager: SensorManager) : SensorEventListener2 {

    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val timers = LongArray(3)
    private val decimalFormat = DecimalFormat("#.####")

    var values = "" // The variable in which the readings will be stored
    var delay = 10 // The delay between two readings in ms.

    override fun onSensorChanged(event: SensorEvent) {
        val index = when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> 0
            Sensor.TYPE_MAGNETIC_FIELD -> 1
            Sensor.TYPE_GYROSCOPE -> 2
            else -> return
        }

        // Once every few ms, take a reading from the sensors and store it in the designated variable
        if (System.currentTimeMillis() - timers[index] > delay) {
            timers[index] = System.currentTimeMillis()
            val type = when (index) {
                0 -> "Accelerometer "
                1 -> "Magnetometer "
                2 -> "Gyroscope "
                else -> return
            }

            values += "${type[0]} "

            for (value in event.values) {
                val roundedValue = decimalFormat.format(value)
                values += "$roundedValue "
            }

            values += "!"
        }
    }

    override fun onFlushCompleted(sensor: Sensor?) {}

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /**
     * Register the sensors needed.
     */
    fun start(enabledSensors: BooleanArray) {
        if (enabledSensors[0]) {
            sensorManager.registerListener(this, accelerometer, SENSOR_DELAY_FASTEST)
        }

        if (enabledSensors[1]) {
            sensorManager.registerListener(this, gyroscope, SENSOR_DELAY_FASTEST)
        }

        if (enabledSensors[2]) {
            sensorManager.registerListener(this, magnetometer, SENSOR_DELAY_FASTEST)
        }
    }

    /**
     * Unregister the sensors
     */
    fun stop() {
        sensorManager.unregisterListener(this)
        if (values.isNotEmpty() && values[values.length - 1] == '!') {
            values = values.substring(0, values.length - 1)
        }
    }

}