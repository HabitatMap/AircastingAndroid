package pl.llp.aircasting.di

import org.mockito.Mockito
import pl.llp.aircasting.di.modules.PermissionsModule
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager

class TestPermissionsModule: PermissionsModule() {
    override fun providesPermissionsManager(): pl.llp.aircasting.util.helpers.permissions.PermissionsManager {
        return Mockito.mock(pl.llp.aircasting.util.helpers.permissions.PermissionsManager::class.java)
    }

    override fun providesBluetoothManager(permissionsManager: pl.llp.aircasting.util.helpers.permissions.PermissionsManager): BluetoothManager {
        return Mockito.mock(BluetoothManager::class.java)
    }
}
