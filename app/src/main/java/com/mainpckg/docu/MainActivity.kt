package com.mainpckg.docu

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var deviceName: String? = null
    private var deviceAddress: String? = null
    private val requestCodeSpeechInput = 100

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val speechButton = findViewById<ImageButton>(R.id.speakImageButton)
        speechButton.setOnClickListener {
            speak() }

        val permissionsList = arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_SCAN)
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
            createConnectThread = CreateInitialThread(bluetoothAdapter, deviceAddress, this)
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


    // @ TODO The Custom Handler for Communicating with the Arduino

    /* On Back, terminate connection */
    override fun onBackPressed() {
        // Terminate Bluetooth Connection and close app
        createConnectThread!!.cancel()
        val a = Intent(Intent.ACTION_MAIN)
        a.addCategory(Intent.CATEGORY_HOME)
        a.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(a)
    }

    // TODO Move to relevant classes (Ensure encapsulation within those Classes)
    /* Companion Object for the Threads*/
    companion object Threading {
        var handler: Handler? = null
        lateinit var mmSocket: BluetoothSocket
        var connectedThread: ConnectedThread? = null
        var createConnectThread: CreateInitialThread? = null
    }
}