package com.mainpckg.docu

import android.content.Context
import android.widget.TextView
import android.widget.LinearLayout
import android.view.ViewGroup
import android.view.LayoutInflater
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class BluetoothDeviceListAdapter(private val context: Context, private val deviceList: List<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var textName: TextView = v.findViewById(R.id.textViewDeviceName)
        var textAddress: TextView = v.findViewById(R.id.textViewDeviceAddress)
        var linearLayout: LinearLayout = v.findViewById(R.id.linearLayoutDeviceInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.device_info_layout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemHolder = holder as ViewHolder
        val deviceInfoModel = deviceList[position] as DeviceModel
        itemHolder.textName.text = deviceInfoModel.deviceName
        itemHolder.textAddress.text = deviceInfoModel.deviceHardwareAddress

        // When a device is selected
        itemHolder.linearLayout.setOnClickListener {
            val intent = Intent(
                context,
                MainActivity::class.java
            )
            // Send device details to the MainActivity
            intent.putExtra("deviceName", deviceInfoModel.deviceName)
            intent.putExtra("deviceAddress", deviceInfoModel.deviceHardwareAddress)

            // Call MainActivity -> @Todo Refactoring
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }
}