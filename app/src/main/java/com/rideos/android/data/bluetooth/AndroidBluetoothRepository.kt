package com.rideos.android.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context

/**
 * Android BLE boundary. Actual GATT/VESC protocol handling is deliberately
 * kept out of this class and will be added in the next hardware stage.
 */
class AndroidBluetoothRepository(context: Context) : BluetoothRepository {
    private val adapter: BluetoothAdapter? =
        context.getSystemService(BluetoothManager::class.java)?.adapter

    @SuppressLint("MissingPermission")
    override fun startScan() {
        // Scanner implementation will be added with runtime permission handling.
        adapter?.bluetoothLeScanner
    }

    override fun stopScan() = Unit

    override fun connect(device: BluetoothDeviceInfo) = Unit

    override fun disconnect() = Unit
}
