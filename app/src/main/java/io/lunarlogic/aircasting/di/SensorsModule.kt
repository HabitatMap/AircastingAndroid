package io.lunarlogic.aircasting.di

import dagger.Module
import dagger.Provides
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.sensor.AirBeamConnectorFactory
import io.lunarlogic.aircasting.models.SessionBuilder
import io.lunarlogic.aircasting.sensor.AirBeamReconnector
import io.lunarlogic.aircasting.sensor.microphone.AudioReader
import javax.inject.Singleton

@Module
open class SensorsModule {
    @Provides
    @Singleton
    open fun providesAirBeamConnectorFactory(
        application: AircastingApplication,
        settings: Settings,
        errorHandler: ErrorHandler
    ): AirBeamConnectorFactory = AirBeamConnectorFactory(application, settings, errorHandler)

    @Provides
    @Singleton
    open fun providesAirBeamReconnector(
        application: AircastingApplication,
        airBeamConnectorFactory: AirBeamConnectorFactory,
        errorHandler: ErrorHandler,
        sessionsRepository: SessionsRepository
    ): AirBeamReconnector = AirBeamReconnector(application, airBeamConnectorFactory, errorHandler, sessionsRepository)

    @Provides
    @Singleton
    fun prodivesSessionBuilder(): SessionBuilder = SessionBuilder()

    @Provides
    @Singleton
    open fun providesAudioReader(): AudioReader = AudioReader()

    @Provides
    @Singleton
    open fun providesSessionRepository(): SessionsRepository = SessionsRepository()
}
