package pl.llp.aircasting.di

import pl.llp.aircasting.bluetooth.BluetoothManager
import pl.llp.aircasting.permissions.PermissionsManager
import org.mockito.Mockito

class TestPermissionsModule: PermissionsModule() {
    override fun providesPermissionsManager(): PermissionsManager {
        return Mockito.mock(PermissionsManager::class.java)
    }

    override fun providesBluetoothManager(permissionsManager: PermissionsManager): BluetoothManager {
        return Mockito.mock(BluetoothManager::class.java)
    }
}
