package pl.llp.aircasting.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.sensor.common.connector.AirBeamConnectorFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamNonSyncable.connector.AirBeam2Connector
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.SyncableAirBeamConfiguratorFactory
import pl.llp.aircasting.util.helpers.sensor.microphone.AudioReader

@Module(includes = [SensorsModule::class])
open class DevicesModule {

    @Provides
    @UserSessionScope
    fun providesAirBeamConnectorFactory(
        applicationContext: Context,
        mErrorHandler: ErrorHandler,
        bluetoothManager: BluetoothManager,
        airBeam2Connector: AirBeam2Connector,
        syncableAirBeamConfiguratorFactory: SyncableAirBeamConfiguratorFactory,
    ): AirBeamConnectorFactory =
        AirBeamConnectorFactory(
            applicationContext,
            mErrorHandler,
            bluetoothManager,
            airBeam2Connector,
            syncableAirBeamConfiguratorFactory
        )

    @Provides
    @UserSessionScope
    fun providesAudioReader(): AudioReader = AudioReader()
}
