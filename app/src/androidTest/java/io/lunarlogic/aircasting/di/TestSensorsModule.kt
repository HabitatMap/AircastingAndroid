package io.lunarlogic.aircasting.di

import androidx.test.espresso.idling.CountingIdlingResource
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.sensor.airbeam2.AirBeam2Configurator
import io.lunarlogic.aircasting.sensor.airbeam2.AirBeam2Connector
import io.lunarlogic.aircasting.sensor.airbeam2.AirBeam2Reader
import io.lunarlogic.aircasting.sensor.microphone.AudioReader

class TestSensorsModule(
    private val app: AircastingApplication
): SensorsModule() {

    override fun providesAirbeam2Connector(
        errorHandler: ErrorHandler,
        airBeamConfigurator: AirBeam2Configurator,
        airBeam2Reader: AirBeam2Reader
    ): AirBeam2Connector {
        return FakeAirBeam2Connector(app, errorHandler, airBeamConfigurator, airBeam2Reader)
    }

    override fun providesAudioReader(): AudioReader = FakeAudioReader(app)
}
