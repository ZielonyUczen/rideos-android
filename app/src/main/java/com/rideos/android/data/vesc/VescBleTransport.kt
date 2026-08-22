package com.rideos.android.data.vesc

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import java.util.UUID

interface VescBleListener {
    fun onConnected()
    fun onDisconnected()
    fun onDataReceived(data: ByteArray)
    fun onError(message: String)
}

/**
 * Minimal GATT transport for the VESC Nordic UART Service.
 * Packet framing and VESC command parsing stay in separate classes.
 */
class VescBleTransport(
    private val context: Context,
    private val listener: VescBleListener
) {
    private var gatt: BluetoothGatt? = null
    private var rx: BluetoothGattCharacteristic? = null

    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice) {
        gatt?.close()
        gatt = device.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        gatt?.disconnect()
    }

    @SuppressLint("MissingPermission")
    fun close() {
        gatt?.close()
        gatt = null
    }

    @SuppressLint("MissingPermission")
    fun write(data: ByteArray): Boolean {
        val characteristic = rx ?: return false
        val connection = gatt ?: return false
        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        return connection.writeCharacteristic(characteristic, data)
    }

    private val callback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED && status == BluetoothGatt.GATT_SUCCESS) {
                this@VescBleTransport.gatt = gatt
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                listener.onDisconnected()
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                listener.onError("GATT connection failed: $status")
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                listener.onError("Service discovery failed: $status")
                return
            }

            val service = gatt.getService(VescBleUuids.SERVICE)
            val rxCharacteristic = service?.getCharacteristic(VescBleUuids.RX)
            val txCharacteristic = service?.getCharacteristic(VescBleUuids.TX)

            if (service == null || rxCharacteristic == null || txCharacteristic == null) {
                listener.onError("VESC Nordic UART service not found")
                return
            }

            rx = rxCharacteristic
            val notificationEnabled = gatt.setCharacteristicNotification(txCharacteristic, true)
            if (!notificationEnabled) {
                listener.onError("Unable to enable VESC TX notifications")
                return
            }

            val descriptor = txCharacteristic.getDescriptor(VescBleUuids.CLIENT_CHARACTERISTIC_CONFIGURATION)
            if (descriptor == null) {
                listener.onError("TX notification descriptor not found")
                return
            }

            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            if (!gatt.writeDescriptor(descriptor)) {
                listener.onError("Unable to configure VESC TX notifications")
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            if (descriptor.uuid == VescBleUuids.CLIENT_CHARACTERISTIC_CONFIGURATION &&
                status == BluetoothGatt.GATT_SUCCESS
            ) {
                listener.onConnected()
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                listener.onError("Notification configuration failed: $status")
            }
        }

        @Deprecated("Compatibility callback for older Android BLE implementations")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == VescBleUuids.TX) {
                listener.onDataReceived(characteristic.value)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            if (characteristic.uuid == VescBleUuids.TX) {
                listener.onDataReceived(value)
            }
        }
    }
}
