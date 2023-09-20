package pl.llp.aircasting.di

import dagger.Module
import dagger.Provides
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.di.mocks.FakeAirBeamConnectorFactory
import pl.llp.aircasting.di.mocks.FakeAudioReader
import pl.llp.aircasting.di.modules.SensorsModule
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.sensor.common.connector.AirBeamConnectorFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamNonSyncable.configurator.AirBeam2Configurator
import pl.llp.aircasting.util.helpers.sensor.airbeamNonSyncable.reader.AirBeam2Reader
import pl.llp.aircasting.util.helpers.sensor.airbeamNonSyncable.connector.AirBeam2Connector
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.SyncableAirBeamConfiguratorFactory
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
        airBeam2Connector: AirBeam2Connector,
    ): AirBeamConnectorFactory {
        return FakeAirBeamConnectorFactory(
            app,
            mErrorHandler,
            bluetoothManager,
            mAirBeamConfigurator,
            mAirBeam2Reader,
            syncableAirBeamConfiguratorFactory,
            airBeam2Connector
        )
    }

    @Provides
    @UserSessionScope
    fun providesAudioReaderTest(application: AircastingApplication): AudioReader =
        FakeAudioReader(application)
}
