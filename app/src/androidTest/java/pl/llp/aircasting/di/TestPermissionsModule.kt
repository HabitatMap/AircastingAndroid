package pl.llp.aircasting.di

import dagger.Module
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.helpers.stubDeviceItem
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager

@Module
class TestPermissionsModule {
    fun providesPermissionsManager(): PermissionsManager {
        return mock {
            on { bluetoothPermissionsGranted(any()) } doReturn true
            on { locationPermissionsGranted(any()) } doReturn true
        }
    }

    fun providesBluetoothManager(
        application: AircastingApplication,
        permissionsManager: PermissionsManager
    ): BluetoothManager {
        return mock {
            on { isBluetoothEnabled() } doReturn true
            on { pairedDeviceItems() } doReturn listOf(stubDeviceItem())
        }
    }
}
