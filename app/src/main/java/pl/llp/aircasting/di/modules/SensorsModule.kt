package pl.llp.aircasting.di.modules

import dagger.Module
import dagger.Provides
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.data.api.services.UploadFixedMeasurementsService
import pl.llp.aircasting.data.api.repositories.MeasurementStreamsRepository
import pl.llp.aircasting.data.api.repositories.MeasurementsRepository
import pl.llp.aircasting.data.api.repositories.SessionsRepository
import pl.llp.aircasting.data.model.SessionBuilder
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.sensor.AirBeamConnectorFactory
import pl.llp.aircasting.util.helpers.sensor.AirBeamDiscoveryService
import pl.llp.aircasting.util.helpers.sensor.AirBeamReconnector
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.*
import pl.llp.aircasting.util.helpers.sensor.microphone.AudioReader
import pl.llp.aircasting.util.helpers.sensor.microphone.MicrophoneReader
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