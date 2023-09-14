package pl.llp.aircasting.di.mocks.sdSync

import android.content.Context
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.sensor.HexMessagesBuilder
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.AirBeam3Reader
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.AirBeamMiniConfigurator
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardReader
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardSyncServiceTest
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardSyncServiceTest.DBStub.ABMiniStubData.measurements1
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardSyncServiceTest.DBStub.ABMiniStubData.measurements2

class TestABMiniConfigurator(
    applicationContext: Context,
    mErrorHandler: ErrorHandler,
    mSettings: Settings,
    hexMessagesBuilder: HexMessagesBuilder,
    airBeam3Reader: AirBeam3Reader,
    private val sdCardReader: SDCardReader,
) : AirBeamMiniConfigurator(
    applicationContext, mErrorHandler, mSettings, hexMessagesBuilder, airBeam3Reader, sdCardReader
) {
    companion object {
        private const val BLE_META_DATA =
            "BLE Entry Count: ${SDCardSyncServiceTest.DBStub.ABMiniStubData.bleCount}"
        private const val WIFI_META_DATA =
            "WiFi Entry Count: ${SDCardSyncServiceTest.DBStub.ABMiniStubData.wifiCount}"
        private const val DOWNLOAD_FINISHED = "SD_SYNC_FINISH"
    }

    override fun triggerSDCardDownload() {
        sdCardReader.onMetaDataDownloaded(BLE_META_DATA.toByteArray())
        sdCardReader.onMeasurementsDownloaded(measurements1.toByteArray())
        sdCardReader.onMeasurementsDownloaded(measurements2.toByteArray())
        sdCardReader.onMetaDataDownloaded(WIFI_META_DATA.toByteArray())
        sdCardReader.onMetaDataDownloaded(DOWNLOAD_FINISHED.toByteArray())
    }
}