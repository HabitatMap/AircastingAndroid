package pl.llp.aircasting.di.modules

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.api.services.FixedSessionUploadService
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.AirbeamConnectionStatus
import pl.llp.aircasting.data.model.SessionBuilder
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.SDCardCSVFileFactory
import pl.llp.aircasting.util.helpers.sensor.common.connector.AirBeamReconnector
import pl.llp.aircasting.util.helpers.sensor.handlers.RecordingHandler
import pl.llp.aircasting.util.helpers.sensor.handlers.RecordingHandlerImpl
import pl.llp.aircasting.util.helpers.sensor.microphone.AudioReader
import pl.llp.aircasting.util.helpers.sensor.microphone.MicrophoneReader
import pl.llp.aircasting.util.helpers.sensor.services.AirBeamDiscoveryService
import pl.llp.aircasting.util.helpers.services.AveragingService
import pl.llp.aircasting.util.helpers.services.MeasurementsAveragingHelper
import pl.llp.aircasting.util.helpers.services.MeasurementsAveragingHelperDefault

@Module
open class SensorsModule {
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
    open fun providesAirBeamReconnector(
        application: AircastingApplication,
        sessionsRepository: SessionsRepository,
        airBeamDiscoveryService: AirBeamDiscoveryService,
        @IoCoroutineScope coroutineScope: CoroutineScope,
        @AirbeamConnectionStatusFlow connectionStatusFlow: StateFlow<AirbeamConnectionStatus?>,
        @SyncActiveFlow syncActiveFlow: SharedFlow<Boolean>
    ): AirBeamReconnector =
        AirBeamReconnector(
            application,
            sessionsRepository,
            airBeamDiscoveryService,
            coroutineScope,
            connectionStatusFlow = connectionStatusFlow,
            syncStatusFlow = syncActiveFlow)

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
