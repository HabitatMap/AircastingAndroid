package pl.llp.aircasting.di

import dagger.Module
import dagger.Provides
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.di.mocks.FakeAirBeamConnectorFactory
import pl.llp.aircasting.di.mocks.FakeAudioReader
import pl.llp.aircasting.di.modules.SensorsModule
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.sensor.AirBeamConnectorFactory
import pl.llp.aircasting.util.helpers.sensor.airbeam2.AirBeam2Configurator
import pl.llp.aircasting.util.helpers.sensor.airbeam2.AirBeam2Reader
import pl.llp.aircasting.util.helpers.sensor.airbeam2.NonSyncableAirBeamConnector
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.SyncableAirBeamConfiguratorFactory
import pl.llp.aircasting.util.helpers.sensor.microphone.AudioReader

@Module(includes = [SensorsModule::class])
open class TestDevicesModule {

    @Provides
    @UserSessionScope
    fun providesAirBeamConnectorFactoryTest(
        app: AircastingApplication,
        mErrorHandler: ErrorHandler,
        bluetoothManager: BluetoothManager,
        mAirBeamConfigurator: AirBeam2Configurator,
        mAirBeam2Reader: AirBeam2Reader,
        syncableAirBeamConfiguratorFactory: SyncableAirBeamConfiguratorFactory,
        nonSyncableAirBeamConnector: NonSyncableAirBeamConnector,
    ): AirBeamConnectorFactory {
        return FakeAirBeamConnectorFactory(
            app,
            mErrorHandler,
            bluetoothManager,
            mAirBeamConfigurator,
            mAirBeam2Reader,
            syncableAirBeamConfiguratorFactory,
            nonSyncableAirBeamConnector
        )
    }

    @Provides
    @UserSessionScope
    fun providesAudioReaderTest(application: AircastingApplication): AudioReader =
        FakeAudioReader(application)
}
