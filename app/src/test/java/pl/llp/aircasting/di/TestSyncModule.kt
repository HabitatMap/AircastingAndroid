package pl.llp.aircasting.di

import android.content.Context
import dagger.Module
import dagger.Provides
import pl.llp.aircasting.di.mocks.sdSync.TestSyncableAirBeamConfiguratorFactory
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.sensor.HexMessagesBuilder
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.AirBeam3Reader
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.SyncableAirBeamConfiguratorFactory
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
        airBeam3Reader: AirBeam3Reader,
        sdCardFileServiceProvider: SDCardFileServiceProvider
    ): SyncableAirBeamConfiguratorFactory = TestSyncableAirBeamConfiguratorFactory(
        applicationContext,
        mErrorHandler,
        mSettings,
        hexMessagesBuilder,
        airBeam3Reader,
        sdCardFileServiceProvider
    )
}