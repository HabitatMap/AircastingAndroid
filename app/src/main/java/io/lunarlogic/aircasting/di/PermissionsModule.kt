package io.lunarlogic.aircasting.di

import dagger.Module
import dagger.Provides
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.permissions.PermissionsActivity
import io.lunarlogic.aircasting.permissions.PermissionsManager
import javax.inject.Singleton

@Module
open class PermissionsModule {
    lateinit var permissionsActivity: PermissionsActivity

    @Provides
    @Singleton
    open fun providesPermissionsManager(): PermissionsManager = PermissionsManager()

    @Provides
    @Singleton
    open fun providesBluetoothManager(permissionsManager: PermissionsManager): BluetoothManager {
        return BluetoothManager(permissionsActivity, permissionsManager)
    }
}