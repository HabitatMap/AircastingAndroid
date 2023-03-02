package pl.llp.aircasting.di.modules

import dagger.Module
import dagger.Provides
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.data.api.services.UploadFixedMeasurementsService
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
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
import pl.llp.aircasting.util.helpers.services.AveragingService
import pl.llp.aircasting.util.helpers.services.MeasurementsAveragingHelper
import pl.llp.aircasting.util.helpers.services.MeasurementsAveragingHelperDefault
import javax.inject.Singleton

@Module
open class SensorsModule {
    @Provides
    @Singleton
    fun providesSDCardDownloadService(
        application: AircastingApplication
    ): SDCardFileService = SDCardFileService(application)

    @Provides
    @Singleton
    fun providesSDCardCSVFileChecker(): SDCardCSVFileChecker = SDCardCSVFileChecker()

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
    fun providesFixedSDCardCSVIterator(
        errorHandler: ErrorHandler
    ): SDCardSessionFileHandlerFixed = SDCardSessionFileHandlerFixed(errorHandler)

    @Provides
    @Singleton
    fun providesMobileSDCardCSVIterator(
        errorHandler: ErrorHandler,
        sessionsRepository: SessionsRepository,
    ): SDCardSessionFileHandlerMobile =
        SDCardSessionFileHandlerMobile(
            errorHandler,
            sessionsRepository,
        )

    @Provides
    @Singleton
    fun providesSDCardUploadFixedMeasurementsService(
        sdCardCSVIterator: SDCardSessionFileHandlerFixed,
        uploadFixedMeasurementsService: UploadFixedMeasurementsService?
    ): SDCardUploadFixedMeasurementsService =
        SDCardUploadFixedMeasurementsService(
            sdCardCSVIterator,
            uploadFixedMeasurementsService
        )

    @Provides
    @Singleton
    fun providesSDCardMobileSessionsProcessor(
        csvIterator: SDCardSessionFileHandlerMobile,
        sessionsRepository: SessionsRepository,
        measurementStreamsRepository: MeasurementStreamsRepository,
        measurementsRepository: MeasurementsRepositoryImpl
    ): SDCardMobileSessionsProcessor = SDCardMobileSessionsProcessor(
        csvIterator,
        sessionsRepository,
        measurementStreamsRepository,
        measurementsRepository
    )

    @Provides
    @Singleton
    fun providesSDCardFixedSessionsProcessor(
        csvFileFactory: SDCardCSVFileFactory,
        csvIterator: SDCardSessionFileHandlerFixed,
        sessionsRepository: SessionsRepository,
        measurementStreamsRepository: MeasurementStreamsRepository,
        measurementsRepository: MeasurementsRepositoryImpl
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
        sdCardFileService: SDCardFileService,
        sdCardCSVFileChecker: SDCardCSVFileChecker,
        sdCardMobileSessionsProcessor: SDCardMobileSessionsProcessor,
        sdCardFixedSessionsProcessor: SDCardFixedSessionsProcessor,
        sessionsSyncService: SessionsSyncService?,
        sdCardUploadFixedMeasurementsService: SDCardUploadFixedMeasurementsService?,
        errorHandler: ErrorHandler
    ): SDCardSyncService = SDCardSyncService(
        sdCardFileService,
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
        errorHandler: ErrorHandler,
        bluetoothManager: BluetoothManager
    ): AirBeamConnectorFactory =
        AirBeamConnectorFactory(application, settings, errorHandler, bluetoothManager)

    @Provides
    @Singleton
    open fun providesAirBeamReconnector(
        application: AircastingApplication,
        sessionsRepository: SessionsRepository,
        airBeamDiscoveryService: AirBeamDiscoveryService
    ): AirBeamReconnector =
        AirBeamReconnector(application, sessionsRepository, airBeamDiscoveryService)

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
    open fun providesMicrophoneReader(
        audioReader: AudioReader,
        errorHandler: ErrorHandler,
        settings: Settings
    ): MicrophoneReader = MicrophoneReader(audioReader, errorHandler, settings)

    @Provides
    @Singleton
    open fun providesAudioReader(): AudioReader = AudioReader()

    @Provides
    @Singleton
    fun providesMeasurementsAveragingHelper(
        helper: MeasurementsAveragingHelperDefault
    ): MeasurementsAveragingHelper = helper
}
