package com.mainpckg.docu

import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class SelectArduinoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.select_arduino_activity)

        val bluetoothManager = BluetoothManager()
        var bluetoothAdapter = bluetoothManager.adapter
    }
}