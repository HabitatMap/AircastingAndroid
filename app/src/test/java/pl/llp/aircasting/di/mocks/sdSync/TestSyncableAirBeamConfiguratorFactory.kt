package pl.llp.aircasting.di.mocks.sdSync

import android.content.Context
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.sensor.HexMessagesBuilder
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.AirBeam3Reader
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.SyncableAirBeamConfigurator
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.SyncableAirBeamConfiguratorFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardReader
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileService.SDCardFileServiceProvider

class TestSyncableAirBeamConfiguratorFactory(
    private val applicationContext: Context,
    private val mErrorHandler: ErrorHandler,
    private val mSettings: Settings,
    private val hexMessagesBuilder: HexMessagesBuilder,
    private val airBeam3Reader: AirBeam3Reader,
    private val sdCardFileServiceProvider: SDCardFileServiceProvider
) : SyncableAirBeamConfiguratorFactory(
    applicationContext,
    mErrorHandler,
    mSettings,
    hexMessagesBuilder,
    airBeam3Reader,
    sdCardFileServiceProvider
) {
    override fun create(type: DeviceItem.Type): SyncableAirBeamConfigurator = when (type) {
        DeviceItem.Type.AIRBEAMMINI -> TestABMiniConfigurator(
            applicationContext,
            mErrorHandler,
            mSettings,
            hexMessagesBuilder,
            airBeam3Reader,
            SDCardReader(
                sdCardFileServiceProvider.get(type)
            )
        )

        else -> TestAB3Configurator(
            applicationContext,
            mErrorHandler,
            mSettings,
            hexMessagesBuilder,
            airBeam3Reader,
            SDCardReader(
                sdCardFileServiceProvider.get(type)
            )
        )
    }
}