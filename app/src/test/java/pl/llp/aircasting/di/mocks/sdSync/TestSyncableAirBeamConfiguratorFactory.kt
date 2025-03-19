package pl.llp.aircasting.di.mocks.sdSync

import android.content.Context
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.RequestQueueCall
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.SyncableAirBeamConfigurator
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.SyncableAirBeamConfiguratorFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.reader.SyncableAirBeamReader
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardReader
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileService.SDCardFileServiceProvider
import pl.llp.aircasting.util.helpers.sensor.common.HexMessagesBuilder

class TestSyncableAirBeamConfiguratorFactory(
    private val applicationContext: Context,
    private val mErrorHandler: ErrorHandler,
    private val mSettings: Settings,
    private val hexMessagesBuilder: HexMessagesBuilder,
    private val syncableAirBeamReader: SyncableAirBeamReader,
    private val sdCardFileServiceProvider: SDCardFileServiceProvider,
    private val await: RequestQueueCall,
) : SyncableAirBeamConfiguratorFactory(
    applicationContext,
    mErrorHandler,
    mSettings,
    hexMessagesBuilder,
    syncableAirBeamReader,
    sdCardFileServiceProvider,
    await
) {
    override fun create(type: DeviceItem.Type): SyncableAirBeamConfigurator = when (type) {
        DeviceItem.Type.AIRBEAMMINI -> TestABMiniConfigurator(
            applicationContext,
            mErrorHandler,
            mSettings,
            hexMessagesBuilder,
            syncableAirBeamReader,
            SDCardReader(
                sdCardFileServiceProvider.get(type)
            ),
            await
        )

        else -> TestAB3Configurator(
            applicationContext,
            mErrorHandler,
            mSettings,
            hexMessagesBuilder,
            syncableAirBeamReader,
            SDCardReader(
                sdCardFileServiceProvider.get(type)
            ),
            await
        )
    }
}