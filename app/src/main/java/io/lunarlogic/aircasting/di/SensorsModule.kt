package io.lunarlogic.aircasting.di

import dagger.Module
import dagger.Provides
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.sensor.AirBeamConnectorFactory
import io.lunarlogic.aircasting.models.SessionBuilder
import io.lunarlogic.aircasting.sensor.AirBeamReconnector
import io.lunarlogic.aircasting.sensor.microphone.AudioReader
import io.lunarlogic.aircasting.sensor.microphone.MicrophoneReader
import io.lunarlogic.aircasting.sensor.microphone.MicrophoneService
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
        sessionsRepository: SessionsRepository,
        bluetoothManager: BluetoothManager
    ): AirBeamReconnector = AirBeamReconnector(application, sessionsRepository, bluetoothManager)

    @Provides
    @Singleton
    fun prodivesSessionBuilder(): SessionBuilder = SessionBuilder()

    @Provides
    @Singleton
    open fun providesMicrophoneReader(audioReader: AudioReader, errorHandler: ErrorHandler, settings: Settings): MicrophoneReader
            = MicrophoneReader(audioReader, errorHandler, settings)

    @Provides
    @Singleton
    open fun providesAudioReader(): AudioReader = AudioReader()

    @Provides
    @Singleton
    open fun providesSessionRepository(): SessionsRepository = SessionsRepository()
}
