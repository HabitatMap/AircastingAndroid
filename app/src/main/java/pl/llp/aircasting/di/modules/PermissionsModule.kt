package pl.llp.aircasting.di.modules

import dagger.Module
import dagger.Provides
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManagerDefault
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothRuntimePermissionManager
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import pl.llp.aircasting.util.isSDKGreaterOrEqualToS
import javax.inject.Singleton

@Module
open class PermissionsModule {
    @Provides
    @UserSessionScope
    open fun providesPermissionsManager(): PermissionsManager = PermissionsManager()

    @Provides
    @UserSessionScope
    open fun providesBluetoothManager(application: AircastingApplication): BluetoothManager {
        return if (isSDKGreaterOrEqualToS())
            BluetoothRuntimePermissionManager(application.applicationContext, providesPermissionsManager())
        else
            BluetoothManagerDefault()
    }
}
