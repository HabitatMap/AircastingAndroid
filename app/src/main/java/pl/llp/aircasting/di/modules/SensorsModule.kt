package pl.llp.aircasting.di.modules

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.api.services.FixedSessionUploadService
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.data.api.services.UploadFixedMeasurementsService
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.SessionBuilder
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.sensor.AirBeamDiscoveryService
import pl.llp.aircasting.util.helpers.sensor.AirBeamReconnector
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.SDCardCSVFileChecker
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.SDCardCSVFileFactory
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.SDCardClearService
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.SDCardFileService
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.SDCardFixedSessionsProcessor
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.SDCardMobileSessionsProcessor
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.SDCardSessionFileHandlerFixed
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.SDCardSessionFileHandlerMobile
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.SDCardSyncService
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.SDCardUploadFixedMeasurementsService
import pl.llp.aircasting.util.helpers.sensor.handlers.RecordingHandler
import pl.llp.aircasting.util.helpers.sensor.handlers.RecordingHandlerImpl
import pl.llp.aircasting.util.helpers.sensor.microphone.AudioReader
import pl.llp.aircasting.util.helpers.sensor.microphone.MicrophoneReader
import pl.llp.aircasting.util.helpers.services.AveragingService
import pl.llp.aircasting.util.helpers.services.MeasurementsAveragingHelper
import pl.llp.aircasting.util.helpers.services.MeasurementsAveragingHelperDefault

@Module
open class SensorsModule {
    @Provides
    @UserSessionScope
    fun providesSDCardDownloadService(
        @IoCoroutineScope coroutineScope: CoroutineScope,
        mCSVFileFactory: SDCardCSVFileFactory,
    ): SDCardFileService = SDCardFileService(coroutineScope, mCSVFileFactory)

    @Provides
    @UserSessionScope
    fun providesSDCardCSVFileChecker(): SDCardCSVFileChecker = SDCardCSVFileChecker()

    @Provides
    @UserSessionScope
    fun providesSDCardCSVFileFactory(
        application: AircastingApplication
    ): SDCardCSVFileFactory =
        SDCardCSVFileFactory(
            application
        )

    @Provides
    @UserSessionScope
    fun providesFixedSDCardCSVIterator(
        errorHandler: ErrorHandler
    ): SDCardSessionFileHandlerFixed = SDCardSessionFileHandlerFixed(errorHandler)

    @Provides
    @UserSessionScope
    fun providesMobileSDCardCSVIterator(
        errorHandler: ErrorHandler,
        sessionsRepository: SessionsRepository,
        helper: MeasurementsAveragingHelper,
        averagingService: AveragingService
    ): SDCardSessionFileHandlerMobile =
        SDCardSessionFileHandlerMobile(
            errorHandler,
            sessionsRepository,
            helper,
            averagingService
        )

    @Provides
    @UserSessionScope
    fun providesSDCardUploadFixedMeasurementsService(
        sdCardCSVIterator: SDCardSessionFileHandlerFixed,
        uploadFixedMeasurementsService: UploadFixedMeasurementsService?
    ): SDCardUploadFixedMeasurementsService =
        SDCardUploadFixedMeasurementsService(
            sdCardCSVIterator,
            uploadFixedMeasurementsService
        )

    @Provides
    @UserSessionScope
    fun providesSDCardMobileSessionsProcessor(
        csvIterator: SDCardSessionFileHandlerMobile,
        sessionsRepository: SessionsRepository,
        measurementStreamsRepository: MeasurementStreamsRepository,
        measurementsRepository: MeasurementsRepositoryImpl,
        settings: Settings
    ): SDCardMobileSessionsProcessor = SDCardMobileSessionsProcessor(
        csvIterator,
        sessionsRepository,
        measurementStreamsRepository,
        measurementsRepository,
        settings
    )

    @Provides
    @UserSessionScope
    fun providesSDCardFixedSessionsProcessor(
        csvIterator: SDCardSessionFileHandlerFixed,
        sessionsRepository: SessionsRepository,
        measurementStreamsRepository: MeasurementStreamsRepository,
        measurementsRepository: MeasurementsRepositoryImpl
    ): SDCardFixedSessionsProcessor = SDCardFixedSessionsProcessor(
        csvIterator,
        sessionsRepository,
        measurementStreamsRepository,
        measurementsRepository
    )

    @Provides
    @UserSessionScope
    fun providesSDCardSyncService(
        sdCardFileService: SDCardFileService,
        sdCardCSVFileChecker: SDCardCSVFileChecker,
        sdCardMobileSessionsProcessor: SDCardMobileSessionsProcessor,
        sdCardFixedSessionsProcessor: SDCardFixedSessionsProcessor,
        sessionsSyncService: SessionsSyncService?,
        sdCardUploadFixedMeasurementsService: SDCardUploadFixedMeasurementsService?,
        errorHandler: ErrorHandler,
        @IoCoroutineScope coroutineScope: CoroutineScope,
    ): SDCardSyncService = SDCardSyncService(
        sdCardFileService,
        sdCardCSVFileChecker,
        sdCardMobileSessionsProcessor,
        sdCardFixedSessionsProcessor,
        sessionsSyncService,
        sdCardUploadFixedMeasurementsService,
        errorHandler,
        coroutineScope
    )

    @Provides
    @UserSessionScope
    fun providesSDCardClearService(): SDCardClearService = SDCardClearService()

    @Provides
    @UserSessionScope
    open fun providesAirBeamReconnector(
        application: AircastingApplication,
        sessionsRepository: SessionsRepository,
        airBeamDiscoveryService: AirBeamDiscoveryService,
        @IoCoroutineScope coroutineScope: CoroutineScope,
    ): AirBeamReconnector =
        AirBeamReconnector(application, sessionsRepository, airBeamDiscoveryService, coroutineScope)

    @Provides
    @UserSessionScope
    open fun providesAirBeamDiscoveryService(
        application: AircastingApplication,
        bluetoothManager: BluetoothManager
    ): AirBeamDiscoveryService = AirBeamDiscoveryService(application, bluetoothManager)

    @Provides
    @UserSessionScope
    fun providesSessionBuilder(): SessionBuilder = SessionBuilder()

    @Provides
    @UserSessionScope
    open fun providesMicrophoneReader(
        audioReader: AudioReader,
        errorHandler: ErrorHandler,
        settings: Settings
    ): MicrophoneReader = MicrophoneReader(audioReader, errorHandler, settings)

    @Provides
    @UserSessionScope
    fun providesMeasurementsAveragingHelper(
        helper: MeasurementsAveragingHelperDefault
    ): MeasurementsAveragingHelper = helper

    @Provides
    @UserSessionScope
    fun providesAveragingService(
        mMeasurementsRepository: MeasurementsRepositoryImpl,
        mMeasurementStreamsRepository: MeasurementStreamsRepository,
        mSessionsRepository: SessionsRepository,
        helper: MeasurementsAveragingHelper,
    ): AveragingService = AveragingService(
        mMeasurementsRepository,
        mMeasurementStreamsRepository,
        mSessionsRepository,
        helper
    )

    @Provides
    @UserSessionScope
    fun providesRecordingHandler(
        settings: Settings,
        fixedSessionUploadService: FixedSessionUploadService,
        sessionsRepository: SessionsRepository,
        activeSessionMeasurementsRepository: ActiveSessionMeasurementsRepository,
        sessionsSyncService: SessionsSyncService,
        errorHandler: ErrorHandler,
        measurementStreamsRepository: MeasurementStreamsRepository,
        measurementsRepository: MeasurementsRepositoryImpl,
        averagingService: AveragingService,
        @IoCoroutineScope coroutineScope: CoroutineScope,
    ): RecordingHandler = RecordingHandlerImpl(
        settings,
        fixedSessionUploadService,
        sessionsRepository,
        activeSessionMeasurementsRepository,
        sessionsSyncService,
        errorHandler,
        measurementStreamsRepository,
        measurementsRepository,
        averagingService,
        coroutineScope,
        mutableMapOf(),
        mutableMapOf()
    )
}
