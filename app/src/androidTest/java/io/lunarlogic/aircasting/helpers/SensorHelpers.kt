package io.lunarlogic.aircasting.helpers

import com.nhaarman.mockito_kotlin.whenever
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import org.mockito.Mockito

class FakeDeviceItem {
    companion object {
        val ID = "0018961070D6"
        val NAME = "AirBeam2"
        val ADDRESS = "00:18:96:10:70:D6"
        val TYPE = DeviceItem.Type.AIRBEAM2
    }
}

fun stubDeviceItem(): DeviceItem {
    val deviceItem = Mockito.mock(DeviceItem::class.java)
    whenever(deviceItem.id).thenReturn(FakeDeviceItem.ID)
    whenever(deviceItem.name).thenReturn(FakeDeviceItem.NAME)
    whenever(deviceItem.address).thenReturn(FakeDeviceItem.ADDRESS)
    whenever(deviceItem.type).thenReturn(FakeDeviceItem.TYPE)
    return deviceItem
}

fun stubPairedDevice(bluetoothManager: BluetoothManager) {
    val deviceItem = stubDeviceItem()
    whenever(bluetoothManager.pairedDeviceItems()).thenReturn(listOf(deviceItem))
}
