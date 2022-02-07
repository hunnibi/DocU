package com.mainpckg.docu

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.mainpckg.docu.MainActivity.Threading.handler
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
    private val mmInStream: InputStream?
    private val mmOutStream: OutputStream?
    private  val MESSAGE_READ = 2
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
            tmpIn = MainActivity.mmSocket.inputStream
            tmpOut = MainActivity.mmSocket.outputStream
        } catch (e: IOException) {
        }
        mmInStream = tmpIn
        mmOutStream = tmpOut
    }
}
