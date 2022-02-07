package com.mainpckg.docu

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import java.io.IOException

class CreateInitialThread (bluetoothAdapter: BluetoothAdapter, address: String?,
                           private val context: Context) :
    Thread() {
    private val CONNECTING_STATUS = 1 //  message status

    @RequiresApi(Build.VERSION_CODES.S)
    override fun run() {
        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADMIN), 101
                )
                Log.e(
                    "Permissions", "Bluetooth Permissions not granted"
                )
            }
            MainActivity.mmSocket.connect()
            Log.e("Status", "Device connected")
            MainActivity.handler?.obtainMessage(CONNECTING_STATUS, 1, -1)?.sendToTarget()
        } catch (connectException: IOException) {
            // Unable to connect; close the socket and return.
            try {
                MainActivity.mmSocket.close()
                Log.e("Status", "Cannot connect to device")
                MainActivity.handler?.obtainMessage(CONNECTING_STATUS, -1, -1)?.sendToTarget()
            } catch (closeException: IOException) {
                Log.e(ContentValues.TAG, "Could not close the client socket", closeException)
            }
            return
        }
        // If succeed -> New thread
        MainActivity.connectedThread = ConnectedThread(MainActivity.mmSocket)
        MainActivity.connectedThread!!.run()
    }

    // Closes the client socket and causes the thread to finish.
    fun cancel() {
        try {
            MainActivity.connectedThread?.cancel()
            MainActivity.mmSocket.close()
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
                    context,
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
            MainActivity.mmSocket = tmp
        }
    }
}
