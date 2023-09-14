package pl.llp.aircasting.utilities

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

fun stubDeviceItem() = DeviceItem(
    null,
    FakeDeviceItem.NAME,
    FakeDeviceItem.ADDRESS,
    FakeDeviceItem.ID,
    FakeDeviceItem.TYPE
)

fun stubPairedDevice(bluetoothManager: BluetoothManager) {
    val deviceItem = stubDeviceItem()
    whenever(bluetoothManager.pairedDeviceItems()).thenReturn(listOf(deviceItem))
}
