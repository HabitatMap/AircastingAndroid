package pl.llp.aircasting.di

import dagger.Module
import dagger.Provides
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import pl.llp.aircasting.utilities.stubDeviceItem

@Module
class TestPermissionsModule {
    @Provides
    @UserSessionScope
    fun providesPermissionsManager(): PermissionsManager {
        return mock {
            on { bluetoothPermissionsGranted(any()) } doReturn true
            on { locationPermissionsGranted(any()) } doReturn true
        }
    }
    @Provides
    @UserSessionScope
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
