package com.mainpckg.docu

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.speech.RecognizerIntent
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {
    private var deviceName: String? = null
    private var deviceAddress: String? = null
    private val requestCodeSpeechInput = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val speechButton = findViewById<ImageButton>(R.id.speakImageButton)
        speechButton.setOnClickListener {
            speak() }

        val permissisionsList = arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_SCAN)
        val bluetoothButton = findViewById<ImageButton>(R.id.bluetoothConnectButton)
        bluetoothButton.setOnClickListener {
            val bluetoothIntent = Intent(this@MainActivity, SelectBluetoothActivity::class.java)
            startActivity(bluetoothIntent)
        }

        // If a bluetooth device has been selected from SelectDeviceActivity
        deviceName = intent.getStringExtra("deviceName")
        if (deviceName != null) {
            // Get the device address to make BT Connection
            deviceAddress = intent.getStringExtra("deviceAddress")

            val bluetoothManager =
                applicationContext.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter
            createConnectThread = CreateConnectThread(bluetoothAdapter, deviceAddress)
            createConnectThread!!.start()
        }
    }



    /* ================= VOICE INPUT ========================*/
    // @Todo move to own class
    private fun speak() {
        val voiceInputIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        voiceInputIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        voiceInputIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de")

        try {
            //if no error -> Display Text
            startActivityForResult(voiceInputIntent, requestCodeSpeechInput)
        } catch (e: Exception) {
            Log.e("VOICE RECORDING", e.message.toString())
        }
    }

    //TODO Should not use startActivity / onActivityResults
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            requestCodeSpeechInput -> {
                if (resultCode == Activity.RESULT_OK && null != data) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    val displayText = findViewById<TextView>(R.id.recordedTextView)
                    Log.e("NEW", "NEW DIARY ENTRY STARTED!")
                    displayText.text = result!![0]
                }
            }
        }
    }

    //@ TODO Move to own class!
    /* ============================ THREADING  =================================== */
    inner class CreateConnectThread(bluetoothAdapter: BluetoothAdapter, address: String?) :
        Thread() {
        @RequiresApi(Build.VERSION_CODES.S)
        override fun run() {
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                if (ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(this@MainActivity,
                        arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADMIN), 101
                    )
                    Log.e(
                        "Permissions", "Bluetooth Permissions not granted"
                    )
                }
                mmSocket.connect()
                Log.e("Status", "Device connected")
                handler?.obtainMessage(CONNECTING_STATUS, 1, -1)?.sendToTarget()
            } catch (connectException: IOException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close()
                    Log.e("Status", "Cannot connect to device")
                    handler?.obtainMessage(CONNECTING_STATUS, -1, -1)?.sendToTarget()
                } catch (closeException: IOException) {
                    Log.e(ContentValues.TAG, "Could not close the client socket", closeException)
                }
                return
            }
            // If succeed -> New thread
            connectedThread = ConnectedThread(mmSocket)
            connectedThread!!.run()
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                connectedThread?.cancel()
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(ContentValues.TAG, "Could not close the client socket or related Thread", e)
            }
        }

        init {
            /* Use a temporary object that is later assigned to mmSocket
            because mmSocket is final.*/
            val bluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
            var tmp: BluetoothSocket? = null
            val uuid = bluetoothDevice.uuids[0].uuid
            try {
                if (ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.e("Permissions", "Bluetooth Permissions not granted")
                }
                tmp = bluetoothDevice.createRfcommSocketToServiceRecord(uuid)
            } catch (e: IOException) {
                Log.e(ContentValues.TAG, "Socket's create() method failed", e)
            }
            if (tmp != null) {
                mmSocket = tmp
            }
        }
    }

    /* =============================== Thread for Data Transfer =========================================== */
    class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?

        override fun run() {
            val buffer = ByteArray(1024) // buffer store for the stream
            var bytes = 0 // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    /*
                    Read from the InputStream from Arduino until termination character is reached.
                    Then send the whole String message to GUI Handler.
                     */
                    buffer[bytes] = mmInStream!!.read().toByte()
                    var readMessage: String
                    if (buffer[bytes].toInt().toChar() == '\n') {
                        readMessage = String(buffer, 0, bytes)
                        Log.e("Arduino Message", readMessage)
                        handler?.obtainMessage(
                            MESSAGE_READ,
                            readMessage
                        )?.sendToTarget()
                        bytes = 0
                    } else {
                        bytes++
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    break
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        fun write(input: String) {
            val bytes = input.toByteArray()
            try {
                mmOutStream!!.write(bytes)
            } catch (e: IOException) {
                Log.e("Send Error", "Unable to send message", e)
            }
        }

        /* Call this from the main activity to shutdown the connection */
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
            }
        }

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            // Get the input and output streams, using temp objects
            try {
                tmpIn = Threading.mmSocket.inputStream
                tmpOut = Threading.mmSocket.outputStream
            } catch (e: IOException) {
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
        }
    }

    /* On Backpress, terminate connection */
    override fun onBackPressed() {
        // Terminate Bluetooth Connection and close app
        createConnectThread!!.cancel()
        val a = Intent(Intent.ACTION_MAIN)
        a.addCategory(Intent.CATEGORY_HOME)
        a.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(a)
    }

    /* Companion Object for the Threads*/
    companion object Threading {
        var handler: Handler? = null
        lateinit var mmSocket: BluetoothSocket
        var connectedThread: ConnectedThread? = null
        var createConnectThread: CreateConnectThread? = null
        private const val CONNECTING_STATUS = 1 //  message status
        private const val MESSAGE_READ = 2 // message update
    }
}