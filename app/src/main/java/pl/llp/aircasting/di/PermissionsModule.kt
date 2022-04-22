package pl.llp.aircasting.di

import android.app.Activity
import dagger.Module
import dagger.Provides
import pl.llp.aircasting.bluetooth.BluetoothManager
import pl.llp.aircasting.bluetooth.BluetoothManagerDefault
import pl.llp.aircasting.bluetooth.BluetoothRuntimePermissionManager
import pl.llp.aircasting.permissions.PermissionsManager
import javax.inject.Singleton

@Module
open class PermissionsModule {
    @Provides
    @Singleton
    open fun providesPermissionsManager(): PermissionsManager = PermissionsManager()

    @Provides
    @Singleton
    open fun providesBluetoothManager(activity: Activity): BluetoothManager {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
            BluetoothRuntimePermissionManager(activity)
        else
            BluetoothManagerDefault()
    }
}
