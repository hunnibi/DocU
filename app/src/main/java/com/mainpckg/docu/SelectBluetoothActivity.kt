package com.mainpckg.docu

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class SelectBluetoothActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.select_bluetooth_activity)

        // Start the BluetoothManager -> Deals with all the Bluetooth stuff
        val bluetoothManager =
            applicationContext.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        // Find all paired devices on the device @TODO Add Discovery (not for prototype)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("Permissions", "Bluetooth Permissions not granted")
        }
        val pairedDevices = bluetoothAdapter.bondedDevices
        val deviceList: MutableList<Any> = ArrayList()
        if (pairedDevices.size > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (device in pairedDevices) {
                val deviceName = device.name
                val deviceHardwareAddress = device.address // MAC address
                val deviceInfoModel = DeviceModel(deviceName, deviceHardwareAddress)
                deviceList.add(deviceInfoModel)
            }
            // Display paired device using recyclerView
            val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewDevice)
            recyclerView.layoutManager = LinearLayoutManager(this)
            val deviceListAdapter = BluetoothDeviceListAdapter(this, deviceList)
            recyclerView.adapter = deviceListAdapter
            recyclerView.itemAnimator = DefaultItemAnimator()
        } else {
            //@Todo Display message if no paired bluetooth device + button for discovery
        }
    }
}