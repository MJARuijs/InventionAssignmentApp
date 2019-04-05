package com.mjaruijs.inventionassignment

import android.content.Context
import android.hardware.SensorManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.net.DatagramSocket
import java.net.InetAddress

class MainActivity : AppCompatActivity() {

    // port for the server. CHANGE THIS ACCORDING TO THE SERVER
    private companion object {
        private const val SERVER_PORT = 8081
    }

    private lateinit var thread: Thread
    private lateinit var sensors: Sensors

    private lateinit var vibrator: Vibrator
    private var started = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set the onClickListeners
        start_button.setOnClickListener {
            onStartClick()
        }

        stop_button.setOnClickListener {
            onStopClick()
        }

        // Initialize the vibration motor, which can be used to let the user know a measurement is starting/stopping
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Create a thread on which a server runs. This server can be used in combination with a "Remote Control" App
        // This Remote Control App can be used to start/stop measurements remotely
        Thread {
            val server = Server(8080, ::onRead)
            server.start()
        }.start()

        getIPAddress()
    }

    /**
     * Ping the Google servers to get the IP address of this phone.
     */
    private fun getIPAddress() {
        Thread {
            val socket = DatagramSocket()
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002)
            ip_address.append(socket.localAddress.hostAddress)
            socket.close()
        }.start()
    }

    /**
     * The onRead callback for the server defined above
     */
    private fun onRead(message: String) {
        if (message.contains("START")) {
            start()
        }
        if (message.contains("STOP")) {
            stop()
        }
    }

    /**
     * When the start button is clicked, disable all ui elements except for the stop-button, and start reading the measurements
     */
    private fun onStartClick() {
        start_button.isEnabled = false
        stop_button.isEnabled = true
        accelerometer_checkbox.isEnabled = false
        magnetometer_checkbox.isEnabled = false
        gyroscope_checkbox.isEnabled = false

        Toast.makeText(applicationContext, "Starting!", Toast.LENGTH_SHORT).show()
        start()
    }

    /**
     * When the stop button is clicked, re-enable the ui elements, except for the stop-button, and stop reading the measurements
     */
    private fun onStopClick() {
        stop_button.isEnabled = false
        start_button.isEnabled = true
        accelerometer_checkbox.isEnabled = true
        magnetometer_checkbox.isEnabled = true
        gyroscope_checkbox.isEnabled = true

        Toast.makeText(applicationContext, "Stopping!", Toast.LENGTH_SHORT).show()
        stop()
    }

    /**
     * If a measurement has not already started, start a new one
     */
    private fun start() {
        if (!started) {

            // Vibrate for a moment to let the user know a measurement is starting

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(400)
            }

            // Wait for the vibration to finish. Otherwise, it might influence the data
            Thread.sleep(500)
            thread = Thread {

                sensors = Sensors(getSystemService(Context.SENSOR_SERVICE) as SensorManager)
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

    /**
     * If a measurement was started, stop it. Also give a vibration to let the user know it was stopped.
     */
    private fun stop() {
        if (started) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(800, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(800)
            }

            thread.join()
            sensors.stop()

            sendData(sensors.values)

            sensors.values = ""
        }

        started = false
    }

    /**
     * Send the measured data to the server.
     */
    private fun sendData(values: String) {
        Thread {
            try {
                val address = server_address.text.toString()
                val client = Client(address, SERVER_PORT)
                client.write(values)
                client.close()
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

}
