package pl.llp.aircasting.di

import android.content.Context
import dagger.Module
import dagger.Provides
import pl.llp.aircasting.di.mocks.sdSync.TestSyncableAirBeamConfiguratorFactory
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.sensor.common.HexMessagesBuilder
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.reader.SyncableAirBeamReader
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.SyncableAirBeamConfiguratorFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileService.SDCardFileServiceProvider

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
        sdCardFileServiceProvider: SDCardFileServiceProvider
    ): SyncableAirBeamConfiguratorFactory = TestSyncableAirBeamConfiguratorFactory(
        applicationContext,
        mErrorHandler,
        mSettings,
        hexMessagesBuilder,
        syncableAirBeamReader,
        sdCardFileServiceProvider
    )
}