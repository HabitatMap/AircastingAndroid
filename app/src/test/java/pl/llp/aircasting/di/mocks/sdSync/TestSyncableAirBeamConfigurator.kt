package pl.llp.aircasting.di.mocks.sdSync

import android.content.Context
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeam3Configurator
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniConfigurator
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.RequestQueueCall
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.reader.SyncableAirBeamReader
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardReader
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardSyncServiceIntegratedTest
import pl.llp.aircasting.util.helpers.sensor.common.HexMessagesBuilder


class TestAB3Configurator(
    applicationContext: Context,
    mErrorHandler: ErrorHandler,
    mSettings: Settings,
    hexMessagesBuilder: HexMessagesBuilder,
    syncableAirBeamReader: SyncableAirBeamReader,
    private val sdCardReader: SDCardReader,
    await: RequestQueueCall,
) : AirBeam3Configurator(
    applicationContext,
    mErrorHandler,
    mSettings,
    hexMessagesBuilder,
    syncableAirBeamReader,
    sdCardReader,
    await
) {
    companion object {
        private const val BLE_META_DATA =
            "BLE Entry Count: ${SDCardSyncServiceIntegratedTest.AB3SDStubData.bleCount}"
        private const val WIFI_META_DATA =
            "WiFi Entry Count: ${SDCardSyncServiceIntegratedTest.AB3SDStubData.wifiCount}"
        private const val CELL_META_DATA =
            "Cell Entry Count: ${SDCardSyncServiceIntegratedTest.AB3SDStubData.cellCount}"
        private const val DOWNLOAD_FINISHED = "SD_SYNC_FINISH"
    }

    override fun triggerSDCardDownload() {
        sdCardReader.onMetaDataDownloaded(BLE_META_DATA.toByteArray())
        sdCardReader.onMeasurementsDownloaded(SDCardSyncServiceIntegratedTest.AB3SDStubData.measurements1.toByteArray())
        sdCardReader.onMeasurementsDownloaded(SDCardSyncServiceIntegratedTest.AB3SDStubData.measurements2.toByteArray())
        sdCardReader.onMetaDataDownloaded(WIFI_META_DATA.toByteArray())
        sdCardReader.onMetaDataDownloaded(CELL_META_DATA.toByteArray())
        sdCardReader.onMetaDataDownloaded(DOWNLOAD_FINISHED.toByteArray())
    }
}

class TestEmptyAB3Configurator(
    applicationContext: Context,
    mErrorHandler: ErrorHandler,
    mSettings: Settings,
    hexMessagesBuilder: HexMessagesBuilder,
    syncableAirBeamReader: SyncableAirBeamReader,
    private val sdCardReader: SDCardReader,
    await: RequestQueueCall,
) : AirBeam3Configurator(
    applicationContext,
    mErrorHandler,
    mSettings,
    hexMessagesBuilder,
    syncableAirBeamReader,
    sdCardReader,
    await
) {
    companion object {
        private const val BLE_META_DATA = "BLE Entry Count: 0"
        private const val WIFI_META_DATA = "WiFi Entry Count: 0"
        private const val CELL_META_DATA = "Cell Entry Count: 0"
        private const val DOWNLOAD_FINISHED = "SD_SYNC_FINISH"
    }

    override fun triggerSDCardDownload() {
        sdCardReader.onMetaDataDownloaded(BLE_META_DATA.toByteArray())
        sdCardReader.onMetaDataDownloaded(WIFI_META_DATA.toByteArray())
        sdCardReader.onMetaDataDownloaded(CELL_META_DATA.toByteArray())
        sdCardReader.onMetaDataDownloaded(DOWNLOAD_FINISHED.toByteArray())
    }
}

class TestABMiniConfigurator(
    applicationContext: Context,
    mErrorHandler: ErrorHandler,
    mSettings: Settings,
    hexMessagesBuilder: HexMessagesBuilder,
    syncableAirBeamReader: SyncableAirBeamReader,
    private val sdCardReader: SDCardReader,
    await: RequestQueueCall,
) : AirBeamMiniConfigurator(
    applicationContext,
    mErrorHandler,
    mSettings,
    hexMessagesBuilder,
    syncableAirBeamReader,
    sdCardReader,
    await
) {
    companion object {
        private const val BLE_META_DATA =
            "BLE Entry Count: ${SDCardSyncServiceIntegratedTest.ABMiniSDStubData.bleCount}"
        private const val WIFI_META_DATA =
            "WiFi Entry Count: ${SDCardSyncServiceIntegratedTest.ABMiniSDStubData.wifiCount}"
        private const val DOWNLOAD_FINISHED = "SD_SYNC_FINISH"
    }

    override fun triggerSDCardDownload() {
        sdCardReader.onMetaDataDownloaded(BLE_META_DATA.toByteArray())
        sdCardReader.onMeasurementsDownloaded(SDCardSyncServiceIntegratedTest.ABMiniSDStubData.measurements1.toByteArray())
        sdCardReader.onMeasurementsDownloaded(SDCardSyncServiceIntegratedTest.ABMiniSDStubData.measurements2.toByteArray())
        sdCardReader.onMetaDataDownloaded(WIFI_META_DATA.toByteArray())
        sdCardReader.onMetaDataDownloaded(DOWNLOAD_FINISHED.toByteArray())
    }
}

class TestEmptyABMiniConfigurator(
    applicationContext: Context,
    mErrorHandler: ErrorHandler,
    mSettings: Settings,
    hexMessagesBuilder: HexMessagesBuilder,
    syncableAirBeamReader: SyncableAirBeamReader,
    private val sdCardReader: SDCardReader,
    await: RequestQueueCall,
) : AirBeamMiniConfigurator(
    applicationContext,
    mErrorHandler,
    mSettings,
    hexMessagesBuilder,
    syncableAirBeamReader,
    sdCardReader,
    await
) {
    companion object {
        private const val BLE_META_DATA = "BLE Entry Count: 0"
        private const val WIFI_META_DATA = "WiFi Entry Count: 0"
        private const val DOWNLOAD_FINISHED = "SD_SYNC_FINISH"
    }

    override fun triggerSDCardDownload() {
        sdCardReader.onMetaDataDownloaded(BLE_META_DATA.toByteArray())
        sdCardReader.onMetaDataDownloaded(WIFI_META_DATA.toByteArray())
        sdCardReader.onMetaDataDownloaded(DOWNLOAD_FINISHED.toByteArray())
    }
}