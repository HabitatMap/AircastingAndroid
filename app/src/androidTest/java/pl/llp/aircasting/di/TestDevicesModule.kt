package pl.llp.aircasting.di

import dagger.Module
import dagger.Provides
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.di.mocks.FakeAirBeamConnectorFactory
import pl.llp.aircasting.di.mocks.FakeAudioReader
import pl.llp.aircasting.di.modules.SensorsModule
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.sensor.AirBeamConnectorFactory
import pl.llp.aircasting.util.helpers.sensor.microphone.AudioReader

@Module(includes = [SensorsModule::class])
open class TestDevicesModule {

    @Provides
    @UserSessionScope
    fun providesAirBeamConnectorFactoryTest(
        application: AircastingApplication,
        settings: Settings,
        errorHandler: ErrorHandler,
        bluetoothManager: BluetoothManager
    ): AirBeamConnectorFactory {
        return FakeAirBeamConnectorFactory(
            application,
            settings,
            errorHandler,
            bluetoothManager
        )
    }

    @Provides
    @UserSessionScope
    fun providesAudioReaderTest(application: AircastingApplication): AudioReader =
        FakeAudioReader(application)
}
