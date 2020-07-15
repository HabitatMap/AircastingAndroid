package io.lunarlogic.aircasting.helpers

import com.nhaarman.mockito_kotlin.whenever
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.screens.new_session.select_device.items.DeviceItem
import org.mockito.Mockito

fun stubBluetooth(bluetoothManager: BluetoothManager) {
    whenever(bluetoothManager.isBluetoothEnabled()).thenReturn(true)
    whenever(bluetoothManager.permissionsGranted()).thenReturn(false)
    whenever(bluetoothManager.requestBluetoothPermissions()).then({})
}

fun stubPairedDevice(bluetoothManager: BluetoothManager, id: String, name: String, address: String) {
    val deviceItem = Mockito.mock(DeviceItem::class.java)
    whenever(deviceItem.id).thenReturn(id)
    whenever(deviceItem.name).thenReturn(name)
    whenever(deviceItem.address).thenReturn(address)
    whenever(bluetoothManager.pairedDeviceItems()).thenReturn(listOf(deviceItem))
}