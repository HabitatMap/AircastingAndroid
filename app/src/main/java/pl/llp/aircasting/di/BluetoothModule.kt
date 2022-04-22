package pl.llp.aircasting.di

import android.app.Activity
import dagger.Module
import dagger.Provides
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.bluetooth.BluetoothManager
import pl.llp.aircasting.bluetooth.BluetoothManagerDefault
import pl.llp.aircasting.bluetooth.BluetoothRuntimePermissionManager
import pl.llp.aircasting.database.repositories.SessionsRepository
import pl.llp.aircasting.screens.main.MainActivity
import pl.llp.aircasting.sensor.AirBeamDiscoveryService
import pl.llp.aircasting.sensor.AirBeamReconnector
import javax.inject.Singleton

@Module
class BluetoothModule {
    @Provides
    @Singleton
    fun providesBluetoothManager(activity: Activity): BluetoothManager {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
            BluetoothRuntimePermissionManager(activity)
        else
            BluetoothManagerDefault()
    }

    @Provides
    @Singleton
    fun providesAirBeamReconnector(
        application: AircastingApplication,
        sessionsRepository: SessionsRepository,
        airBeamDiscoveryService: AirBeamDiscoveryService
    ): AirBeamReconnector =
        AirBeamReconnector(application, sessionsRepository, airBeamDiscoveryService)

    @Provides
    @Singleton
    fun providesAirBeamDiscoveryService(
        application: AircastingApplication,
        bluetoothManager: BluetoothManager
    ): AirBeamDiscoveryService = AirBeamDiscoveryService(application, bluetoothManager)
}