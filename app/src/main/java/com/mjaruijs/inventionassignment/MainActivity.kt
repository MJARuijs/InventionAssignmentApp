package com.mjaruijs.inventionassignment

import android.content.Context
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var thread: Thread
    private lateinit var sensors: Sensors

    private lateinit var vibrator: Vibrator
    private var started = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        start_button.setOnClickListener {
            onStartClick()
        }

        stop_button.setOnClickListener {
            onStopClick()
        }

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        Thread {
            val server = Server(8080, ::onRead)
            server.start()
        }.start()
    }

    private fun onRead(message: String) {
        if (message.contains("START")) {
            start()
        }
        if (message.contains("STOP")) {
            stop()
        }
    }

    private fun onStartClick() {
        start_button.isEnabled = false
        stop_button.isEnabled = true
        accelerometer_checkbox.isEnabled = false
        magnetometer_checkbox.isEnabled = false
        gyroscope_checkbox.isEnabled = false
        stream_checkbox.isEnabled = false

        Toast.makeText(applicationContext, "Starting!", Toast.LENGTH_SHORT).show()

//        if (start_delay.text?.isNotEmpty()!!) {
//            val startDelay = start_delay.text.toString().toLong()
//            Thread.sleep(startDelay)
//        }

        start()
    }

    private fun onStopClick() {
        stop_button.isEnabled = false
        start_button.isEnabled = true
        accelerometer_checkbox.isEnabled = true
        magnetometer_checkbox.isEnabled = true
        gyroscope_checkbox.isEnabled = true
        stream_checkbox.isEnabled = true

        Toast.makeText(applicationContext, "Stopping!", Toast.LENGTH_SHORT).show()

        stop()
    }

    private fun start() {
        if (!started) {
            if (start_delay.text?.isNotEmpty()!!) {
                val startDelay = start_delay.text.toString().toLong()
                Thread.sleep(startDelay)
            }
            vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))

            Thread.sleep(400)
            thread = Thread {
                sensors = Sensors(getSystemService(Context.SENSOR_SERVICE) as SensorManager, null)
                sensors.delay = delay.text.toString().toInt()
                sensors.values += "D${sensors.delay}!"
                sensors.values += "N${name_input.text.toString()}!"
                sensors.start(booleanArrayOf(
                    accelerometer_checkbox.isChecked,
                    gyroscope_checkbox.isChecked,
                    magnetometer_checkbox.isChecked
                ))
            }
            thread.start()

            started = true
        }
    }

    private fun stop() {
        if (started) {
            vibrator.vibrate(VibrationEffect.createOneShot(800, VibrationEffect.DEFAULT_AMPLITUDE))

            thread.join()
            sensors.stop()

            sendData(sensors.values)

            sensors.values = ""
        }

        started = false
    }

    private fun sendData(values: String) {
        Thread {
            try {
                val client = Client("192.168.178.18", 8081)
                client.write(values)
                client.close()
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

}
