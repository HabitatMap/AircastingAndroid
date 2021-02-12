package io.lunarlogic.aircasting.di

import dagger.Module
import dagger.Provides
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.database.repositories.MeasurementStreamsRepository
import io.lunarlogic.aircasting.database.repositories.MeasurementsRepository
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.models.SessionBuilder
import io.lunarlogic.aircasting.networking.services.SessionsSyncService
import io.lunarlogic.aircasting.sensor.*
import io.lunarlogic.aircasting.sensor.airbeam3.sync.*
import io.lunarlogic.aircasting.sensor.microphone.AudioReader
import io.lunarlogic.aircasting.sensor.microphone.MicrophoneReader
import javax.inject.Singleton

@Module
open class SensorsModule {
    @Provides
    @Singleton
    fun providesSDCardDownloadService(
        application: AircastingApplication
    ): SDCardDownloadService = SDCardDownloadService(application)

    @Provides
    @Singleton
    fun providesSDCardCSVFileChecker(
        application: AircastingApplication
    ): SDCardCSVFileChecker =
        SDCardCSVFileChecker(
            application
        )

    @Provides
    @Singleton
    fun providesSDCardMeasurementsCreator(
        application: AircastingApplication,
        errorHandler: ErrorHandler,
        sessionsRepository: SessionsRepository,
        measurementStreamsRepository: MeasurementStreamsRepository,
        measurementsRepository: MeasurementsRepository
    ): SDCardMeasurementsCreator = SDCardMeasurementsCreator(
        application,
        errorHandler,
        sessionsRepository,
        measurementStreamsRepository,
        measurementsRepository
    )

    @Provides
    @Singleton
    fun providesSDCardSyncService(
        sdCardDownloadService: SDCardDownloadService,
        sdCardCSVFileChecker: SDCardCSVFileChecker,
        sdCardMeasurementsCreator: SDCardMeasurementsCreator,
        sessionsSyncService: SessionsSyncService?
    ): SDCardSyncService = SDCardSyncService(
        sdCardDownloadService,
        sdCardCSVFileChecker,
        sdCardMeasurementsCreator,
        sessionsSyncService
    )

    @Provides
    @Singleton
    fun providesSDCardClearService(): SDCardClearService = SDCardClearService()

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
        sessionsRepository: SessionsRepository,
        airBeamDiscoveryService: AirBeamDiscoveryService
    ): AirBeamReconnector = AirBeamReconnector(application, sessionsRepository, airBeamDiscoveryService)

    @Provides
    @Singleton
    open fun providesAirBeamDiscoveryService(
        application: AircastingApplication,
        bluetoothManager: BluetoothManager
    ): AirBeamDiscoveryService = AirBeamDiscoveryService(application, bluetoothManager)

    @Provides
    @Singleton
    fun providesSessionBuilder(): SessionBuilder = SessionBuilder()

    @Provides
    @Singleton
    open fun providesMicrophoneReader(audioReader: AudioReader, errorHandler: ErrorHandler, settings: Settings): MicrophoneReader
            = MicrophoneReader(audioReader, errorHandler, settings)

    @Provides
    @Singleton
    open fun providesAudioReader(): AudioReader = AudioReader()
}
