package io.lunarlogic.aircasting.bluetooth

import io.lunarlogic.aircasting.permissions.PermissionsManager

interface BluetoothActivity {
    fun bluetoothPermissionsGranted(permissionsManager: PermissionsManager) : Boolean
    fun requestBluetoothPermissions(permissionsManager: PermissionsManager)
    fun requestBluetoothEnable()
    fun registerBluetoothDeviceFoundReceiver(receiver: BluetoothDeviceFoundReceiver)
    fun unregisterBluetoothDeviceFoundReceiver(receiver: BluetoothDeviceFoundReceiver)
}