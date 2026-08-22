package com.rideos.android.data.bluetooth

/** Device metadata exposed to the UI without leaking Android BLE types. */
data class BluetoothDeviceInfo(
    val name: String,
    val address: String,
    val rssi: Int
)

interface BluetoothRepository {
    fun startScan()
    fun stopScan()
    fun connect(device: BluetoothDeviceInfo)
    fun disconnect()
}
