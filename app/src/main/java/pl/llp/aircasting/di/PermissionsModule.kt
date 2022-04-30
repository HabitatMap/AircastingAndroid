package pl.llp.aircasting.di

import dagger.Module
import dagger.Provides
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.bluetooth.BluetoothManager
import pl.llp.aircasting.bluetooth.BluetoothManagerDefault
import pl.llp.aircasting.bluetooth.BluetoothRuntimePermissionManager
import pl.llp.aircasting.util.isSDKGreaterOrEqualToS
import javax.inject.Singleton

@Module
open class PermissionsModule {
    @Provides
    @Singleton
    open fun providesPermissionsManager(): PermissionsManager = PermissionsManager()

    @Provides
    @Singleton
    open fun providesBluetoothManager(application: AircastingApplication): BluetoothManager {
        return if (isSDKGreaterOrEqualToS())
            BluetoothRuntimePermissionManager(application.applicationContext)
        else
            BluetoothManagerDefault()
    }
}
