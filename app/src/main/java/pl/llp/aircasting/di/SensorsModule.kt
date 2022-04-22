package pl.llp.aircasting.di

import dagger.Module
import dagger.Provides
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.bluetooth.BluetoothManager
import pl.llp.aircasting.database.repositories.MeasurementStreamsRepository
import pl.llp.aircasting.database.repositories.MeasurementsRepository
import pl.llp.aircasting.database.repositories.SessionsRepository
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.models.SessionBuilder
import pl.llp.aircasting.networking.services.SessionsSyncService
import pl.llp.aircasting.networking.services.UploadFixedMeasurementsService
import pl.llp.aircasting.sensor.AirBeamConnectorFactory
import pl.llp.aircasting.sensor.AirBeamDiscoveryService
import pl.llp.aircasting.sensor.AirBeamReconnector
import pl.llp.aircasting.sensor.airbeam3.sync.*
import pl.llp.aircasting.sensor.microphone.AudioReader
import pl.llp.aircasting.sensor.microphone.MicrophoneReader
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
        sdCardCSVFileFactory: SDCardCSVFileFactory
    ): SDCardCSVFileChecker =
        SDCardCSVFileChecker(
            sdCardCSVFileFactory
        )

    @Provides
    @Singleton
    fun providesSDCardCSVFileFactory(
        application: AircastingApplication
    ): SDCardCSVFileFactory =
        SDCardCSVFileFactory(
            application
        )

    @Provides
    @Singleton
    fun providesSDCardCSVIterator(
        errorHandler: ErrorHandler
    ): SDCardCSVIterator =
        SDCardCSVIterator(
            errorHandler
        )

    @Provides
    @Singleton
    fun providesSDCardUploadFixedMeasurementsService(
        sdCardCSVFileFactory: SDCardCSVFileFactory,
        sdCardCSVIterator: SDCardCSVIterator,
        uploadFixedMeasurementsService: UploadFixedMeasurementsService?
    ): SDCardUploadFixedMeasurementsService =
        SDCardUploadFixedMeasurementsService(
            sdCardCSVFileFactory,
            sdCardCSVIterator,
            uploadFixedMeasurementsService
        )

    @Provides
    @Singleton
    fun providesSDCardMobileSessionsProcessor(
        csvFileFactory: SDCardCSVFileFactory,
        csvIterator: SDCardCSVIterator,
        sessionsRepository: SessionsRepository,
        measurementStreamsRepository: MeasurementStreamsRepository,
        measurementsRepository: MeasurementsRepository
    ): SDCardMobileSessionsProcessor = SDCardMobileSessionsProcessor(
        csvFileFactory,
        csvIterator,
        sessionsRepository,
        measurementStreamsRepository,
        measurementsRepository
    )

    @Provides
    @Singleton
    fun providesSDCardFixedSessionsProcessor(
        csvFileFactory: SDCardCSVFileFactory,
        csvIterator: SDCardCSVIterator,
        sessionsRepository: SessionsRepository,
        measurementStreamsRepository: MeasurementStreamsRepository,
        measurementsRepository: MeasurementsRepository
    ): SDCardFixedSessionsProcessor = SDCardFixedSessionsProcessor(
        csvFileFactory,
        csvIterator,
        sessionsRepository,
        measurementStreamsRepository,
        measurementsRepository
    )

    @Provides
    @Singleton
    fun providesSDCardSyncService(
        sdCardDownloadService: SDCardDownloadService,
        sdCardCSVFileChecker: SDCardCSVFileChecker,
        sdCardMobileSessionsProcessor: SDCardMobileSessionsProcessor,
        sdCardFixedSessionsProcessor: SDCardFixedSessionsProcessor,
        sessionsSyncService: SessionsSyncService?,
        sdCardUploadFixedMeasurementsService: SDCardUploadFixedMeasurementsService?,
        errorHandler: ErrorHandler
    ): SDCardSyncService = SDCardSyncService(
        sdCardDownloadService,
        sdCardCSVFileChecker,
        sdCardMobileSessionsProcessor,
        sdCardFixedSessionsProcessor,
        sessionsSyncService,
        sdCardUploadFixedMeasurementsService,
        errorHandler
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
    fun providesSessionBuilder(): SessionBuilder = SessionBuilder()

    @Provides
    @Singleton
    open fun providesMicrophoneReader(audioReader: AudioReader, errorHandler: ErrorHandler, settings: Settings): MicrophoneReader
            = MicrophoneReader(audioReader, errorHandler, settings)

    @Provides
    @Singleton
    open fun providesAudioReader(): AudioReader = AudioReader()
}
