package io.lunarlogic.aircasting.di

import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.permissions.PermissionsManager
import org.mockito.Mockito

class TestPermissionsModule: PermissionsModule() {
    override fun providesBluetoothManager(permissionsManager: PermissionsManager): BluetoothManager {
        return Mockito.mock(BluetoothManager::class.java)
    }
}