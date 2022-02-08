package com.mainpckg.docu

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.util.Log
import androidx.core.app.ActivityCompat
import java.io.IOException

class CreateInitialThread (bluetoothAdapter: BluetoothAdapter, address: String?,
                           private val context: Context) :
    Thread() {
    private val CONNECTING_STATUS = 1 //  message status
    private lateinit var mmSocket: BluetoothSocket
    private lateinit var connectedThread: ConnectedThread
    lateinit var handler: Handler

    override fun run() {
        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_ADMIN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.BLUETOOTH_ADMIN), 101
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
        connectedThread.run()
    }

    // Closes the client socket and causes the thread to finish.
    fun cancel() {
        try {
            connectedThread.cancel()
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
                    context,
                    Manifest.permission.BLUETOOTH_ADMIN
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
