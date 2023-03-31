package pl.llp.aircasting.di.modules

import dagger.Module
import dagger.Provides
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.sensor.AirBeamConnectorFactory
import pl.llp.aircasting.util.helpers.sensor.microphone.AudioReader

@Module(includes = [SensorsModule::class])
open class DevicesModule {

    @Provides
    @UserSessionScope
    fun providesAirBeamConnectorFactory(
        application: AircastingApplication,
        settings: Settings,
        errorHandler: ErrorHandler,
        bluetoothManager: BluetoothManager
    ): AirBeamConnectorFactory =
        AirBeamConnectorFactory(application, settings, errorHandler, bluetoothManager)

    @Provides
    @UserSessionScope
    fun providesAudioReader(): AudioReader = AudioReader()
}
