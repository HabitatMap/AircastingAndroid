package io.lunarlogic.aircasting.di

import dagger.Module
import dagger.Provides
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.sensor.SessionBuilder
import io.lunarlogic.aircasting.sensor.airbeam2.AirBeam2Configurator
import io.lunarlogic.aircasting.sensor.airbeam2.AirBeam2Connector
import io.lunarlogic.aircasting.sensor.airbeam2.AirBeam2Reader
import io.lunarlogic.aircasting.sensor.microphone.AudioReader
import javax.inject.Singleton

@Module
open class SensorsModule {
    @Provides
    @Singleton
    open fun providesAirbeam2Connector(
        errorHandler: ErrorHandler,
        airBeamConfigurator: AirBeam2Configurator,
        airBeam2Reader: AirBeam2Reader
    ): AirBeam2Connector = AirBeam2Connector(errorHandler, airBeamConfigurator, airBeam2Reader)

    @Provides
    @Singleton
    fun prodivesAirbeam2Configurator(settings: Settings): AirBeam2Configurator = AirBeam2Configurator(settings)

    @Provides
    @Singleton
    fun prodivesAirbeam2Reader(errorHandler: ErrorHandler): AirBeam2Reader = AirBeam2Reader(errorHandler)

    @Provides
    @Singleton
    fun prodivesSessionBuilder(): SessionBuilder = SessionBuilder()

    @Provides
    @Singleton
    open fun providesAudioReader(): AudioReader = AudioReader()
}
