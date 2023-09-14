package pl.llp.aircasting.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.sensor.HexMessagesBuilder
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.AirBeam3Reader
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.SyncableAirBeamConfiguratorFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileService.SDCardFileServiceProvider

@Module
object SyncModule {
    @UserSessionScope
    @Provides
    fun provideSyncableAirBeamConfiguratorFactory(
        applicationContext: Context,
        mErrorHandler: ErrorHandler,
        mSettings: Settings,
        hexMessagesBuilder: HexMessagesBuilder,
        airBeam3Reader: AirBeam3Reader,
        sdCardFileServiceProvider: SDCardFileServiceProvider
    ): SyncableAirBeamConfiguratorFactory = SyncableAirBeamConfiguratorFactory(
        applicationContext,
        mErrorHandler,
        mSettings,
        hexMessagesBuilder,
        airBeam3Reader,
        sdCardFileServiceProvider
    )
}