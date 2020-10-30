package io.lunarlogic.aircasting.di

import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.sensor.AirBeamConnectorFactory
import io.lunarlogic.aircasting.sensor.microphone.AudioReader

class TestSensorsModule(
    private val app: AircastingApplication
): SensorsModule() {

    override fun providesAirBeamConnectorFactory(
        application: AircastingApplication,
        settings: Settings,
        errorHandler: ErrorHandler
    ): AirBeamConnectorFactory {
        return FakeAirBeamConnectorFactory(app, settings, errorHandler)
    }

    override fun providesAudioReader(): AudioReader = FakeAudioReader(app)
}
