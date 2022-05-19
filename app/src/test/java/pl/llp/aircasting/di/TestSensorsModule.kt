package pl.llp.aircasting.di

import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.di.mocks.FakeAirBeamConnectorFactory
import pl.llp.aircasting.di.mocks.FakeAudioReader
import pl.llp.aircasting.di.modules.SensorsModule
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.sensor.AirBeamConnectorFactory
import pl.llp.aircasting.util.helpers.sensor.microphone.AudioReader

class TestSensorsModule(
    private val app: AircastingApplication
): SensorsModule() {

    override fun providesAirBeamConnectorFactory(
        application: AircastingApplication,
        settings: Settings,
        errorHandler: ErrorHandler
    ): AirBeamConnectorFactory {
        return FakeAirBeamConnectorFactory(
            app,
            settings,
            errorHandler
        )
    }

    override fun providesAudioReader(): AudioReader =
        FakeAudioReader(app)
}
