package pl.llp.aircasting.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2StateRepository
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.RequestQueueCall
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.SyncableAirBeamConfiguratorFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.reader.SyncableAirBeamReader
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileService.SDCardFileServiceProvider
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.v2.V2SyncOrchestrator
import pl.llp.aircasting.util.helpers.sensor.common.HexMessagesBuilder

@Module
object SyncModule {
    @UserSessionScope
    @Provides
    fun provideSyncableAirBeamConfiguratorFactory(
        applicationContext: Context,
        mErrorHandler: ErrorHandler,
        mSettings: Settings,
        hexMessagesBuilder: HexMessagesBuilder,
        syncableAirBeamReader: SyncableAirBeamReader,
        sdCardFileServiceProvider: SDCardFileServiceProvider,
        await: RequestQueueCall.Await,
        @IoCoroutineScope coroutineScope: CoroutineScope,
        @BatteryLevelFlow batteryLevelFlow: MutableSharedFlow<Int>,
        sessionsRepository: SessionsRepository,
        measurementStreamsRepository: MeasurementStreamsRepository,
        measurementsRepository: MeasurementsRepository,
        activeSessionMeasurementsRepository: ActiveSessionMeasurementsRepository,
        v2StateRepository: AirBeamMiniV2StateRepository,
        v2SyncOrchestrator: V2SyncOrchestrator,
    ): SyncableAirBeamConfiguratorFactory = SyncableAirBeamConfiguratorFactory(
        applicationContext,
        mErrorHandler,
        mSettings,
        hexMessagesBuilder,
        syncableAirBeamReader,
        sdCardFileServiceProvider,
        await,
        coroutineScope,
        batteryLevelFlow,
        sessionsRepository,
        measurementStreamsRepository,
        measurementsRepository,
        activeSessionMeasurementsRepository,
        v2StateRepository,
        v2SyncOrchestrator,
    )
}