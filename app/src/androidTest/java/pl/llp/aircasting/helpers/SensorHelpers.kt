package pl.llp.aircasting.helpers

import org.mockito.Mockito
import org.mockito.kotlin.whenever
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager

class FakeDeviceItem {
    companion object {
        val ID = "0018961070D6"
        val NAME = "AirBeam2:0018961070D6"
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
