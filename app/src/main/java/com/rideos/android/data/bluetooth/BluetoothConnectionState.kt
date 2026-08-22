package com.rideos.android.data.bluetooth

sealed interface BluetoothConnectionState {
    data object Disconnected : BluetoothConnectionState
    data object Scanning : BluetoothConnectionState
    data object Connecting : BluetoothConnectionState
    data class Connected(val deviceName: String, val address: String) : BluetoothConnectionState
    data class Error(val message: String) : BluetoothConnectionState
}
