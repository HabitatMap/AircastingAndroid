package pl.llp.aircasting.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.sensor.AirBeamConnectorFactory
import pl.llp.aircasting.util.helpers.sensor.airbeam2.NonSyncableAirBeamConnector
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.SyncableAirBeamConfiguratorFactory
import pl.llp.aircasting.util.helpers.sensor.microphone.AudioReader

@Module(includes = [SensorsModule::class])
open class DevicesModule {

    @Provides
    @UserSessionScope
    fun providesAirBeamConnectorFactory(
        applicationContext: Context,
        mErrorHandler: ErrorHandler,
        bluetoothManager: BluetoothManager,
        nonSyncableAirBeamConnector: NonSyncableAirBeamConnector,
        syncableAirBeamConfiguratorFactory: SyncableAirBeamConfiguratorFactory,
    ): AirBeamConnectorFactory =
        AirBeamConnectorFactory(
            applicationContext,
            mErrorHandler,
            bluetoothManager,
            nonSyncableAirBeamConnector,
            syncableAirBeamConfiguratorFactory
        )

    @Provides
    @UserSessionScope
    fun providesAudioReader(): AudioReader = AudioReader()
}
