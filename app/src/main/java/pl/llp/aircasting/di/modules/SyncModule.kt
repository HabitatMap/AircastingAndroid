package pl.llp.aircasting.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.SyncableAirBeamConfiguratorFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.reader.SyncableAirBeamReader
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileService.SDCardFileServiceProvider
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
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): SyncableAirBeamConfiguratorFactory = SyncableAirBeamConfiguratorFactory(
        applicationContext,
        mErrorHandler,
        mSettings,
        hexMessagesBuilder,
        syncableAirBeamReader,
        sdCardFileServiceProvider,
        ioDispatcher
    )
}