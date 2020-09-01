package io.lunarlogic.aircasting.di

import dagger.Module
import dagger.Provides
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.permissions.PermissionsManager
import javax.inject.Singleton

@Module
open class PermissionsModule {
    @Provides
    @Singleton
    open fun providesPermissionsManager(): PermissionsManager = PermissionsManager()

    @Provides
    @Singleton
    open fun providesBluetoothManager(permissionsManager: PermissionsManager): BluetoothManager {
        return BluetoothManager()
    }
}
