package pl.llp.aircasting.di

import android.content.Context
import dagger.Module
import dagger.Provides
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.di.mocks.sdSync.TestEmptySyncableAirBeamConfiguratorFactory
import pl.llp.aircasting.di.mocks.sdSync.TestSyncableAirBeamConfiguratorFactory
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.RequestQueueCall
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.SyncableAirBeamConfiguratorFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.reader.SyncableAirBeamReader
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileService.SDCardFileServiceProvider
import pl.llp.aircasting.util.helpers.sensor.common.HexMessagesBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Qualifier

@Module
object TestSyncModule {
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
        sessionsRepository: SessionsRepository,
        measurementStreamsRepository: MeasurementStreamsRepository,
        measurementsRepository: MeasurementsRepository,
    ): SyncableAirBeamConfiguratorFactory = TestSyncableAirBeamConfiguratorFactory(
        applicationContext,
        mErrorHandler,
        mSettings,
        hexMessagesBuilder,
        syncableAirBeamReader,
        sdCardFileServiceProvider,
        await,
        CoroutineScope(Dispatchers.Main),
        MutableSharedFlow(),
        sessionsRepository,
        measurementStreamsRepository,
        measurementsRepository,
    )

    @UserSessionScope
    @Provides
    @EmptySDMeasurements
    fun provideEmptySyncableAirBeamConfiguratorFactory(
        applicationContext: Context,
        mErrorHandler: ErrorHandler,
        mSettings: Settings,
        hexMessagesBuilder: HexMessagesBuilder,
        syncableAirBeamReader: SyncableAirBeamReader,
        sdCardFileServiceProvider: SDCardFileServiceProvider,
        await: RequestQueueCall.Await,
        sessionsRepository: SessionsRepository,
        measurementStreamsRepository: MeasurementStreamsRepository,
        measurementsRepository: MeasurementsRepository,
    ): SyncableAirBeamConfiguratorFactory = TestEmptySyncableAirBeamConfiguratorFactory(
        applicationContext,
        mErrorHandler,
        mSettings,
        hexMessagesBuilder,
        syncableAirBeamReader,
        sdCardFileServiceProvider,
        await,
        CoroutineScope(Dispatchers.Main),
        MutableSharedFlow(),
        sessionsRepository,
        measurementStreamsRepository,
        measurementsRepository,
    )

    @Qualifier
    internal annotation class EmptySDMeasurements
}