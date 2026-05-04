package pl.llp.aircasting.util.helpers.sensor.services

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.di.modules.IoCoroutineScope
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.events.sdcard.SDCardSyncFinished
import pl.llp.aircasting.util.exceptions.AirbeamServiceError
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2StateRepository
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.connector.AirBeamMiniFallbackConnector
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardSessionFileHandlerFixedFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardSessionFileHandlerMobileFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardSyncService
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardSyncServiceFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardUploadFixedMeasurementsServiceFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileChecker.SDCardCSVFileCheckerFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileService.SDCardFileServiceProvider
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.lineParameter.CSVLineParameterHandlerFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.sessionProcessor.SDCardFixedSessionsProcessorFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.sessionProcessor.SDCardMobileSessionsProcessorFactory
import javax.inject.Inject

class AirBeamSyncService : AirBeamService() {
    @Inject
    lateinit var airBeamDiscoveryService: AirBeamDiscoveryService

    @Inject
    lateinit var sdCardSyncServiceFactory: SDCardSyncServiceFactory

    @Inject
    lateinit var fixedSessionsProcessorFactory: SDCardFixedSessionsProcessorFactory

    @Inject
    lateinit var mobileSessionsProcessorFactory: SDCardMobileSessionsProcessorFactory

    @Inject
    lateinit var fixedFileHandlerFactory: SDCardSessionFileHandlerFixedFactory

    @Inject
    lateinit var mobileFileHandlerFactory: SDCardSessionFileHandlerMobileFactory

    @Inject
    lateinit var sDCardUploadFixedMeasurementsServiceFactory: SDCardUploadFixedMeasurementsServiceFactory

    @Inject
    lateinit var sDCardFileServiceProvider: SDCardFileServiceProvider

    @Inject
    lateinit var v2StateRepository: AirBeamMiniV2StateRepository

    @field:[Inject IoCoroutineScope]
    lateinit var ioScope: CoroutineScope

    private lateinit var sdCardSyncService: SDCardSyncService

    companion object {
        const val DEVICE_ITEM_KEY = "inputExtraDeviceItem"
        private const val UUID_EXTRA_KEY = "sessionUuid"

        fun startService(context: Context, deviceItem: DeviceItem, sessionUUID: String?) {
            val startIntent = Intent(context, AirBeamSyncService::class.java)
                .putExtra(DEVICE_ITEM_KEY, deviceItem as Parcelable)
                .putExtra(UUID_EXTRA_KEY, sessionUUID)

            ContextCompat.startForegroundService(context, startIntent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        (application as AircastingApplication).userDependentComponent?.inject(this)
        intent?.extras?.getParcelable<DeviceItem>(DEVICE_ITEM_KEY)?.let { deviceItem ->
            val csvLineParameterHandler = CSVLineParameterHandlerFactory.create(deviceItem.type)
            val csvFileChecker = SDCardCSVFileCheckerFactory.create(deviceItem.type)
            val sDCardFileService = sDCardFileServiceProvider.get(deviceItem.type)
            val mobileFileHandler =
                mobileFileHandlerFactory.create(csvLineParameterHandler, csvFileChecker, deviceItem.type)
            val fixedFileHandler =
                fixedFileHandlerFactory.create(csvLineParameterHandler, csvFileChecker)
            val mobileSessionsProcessor = mobileSessionsProcessorFactory.create(
                csvLineParameterHandler,
                mobileFileHandler
            )
            val fixedSessionsProcessor = fixedSessionsProcessorFactory.create(
                csvLineParameterHandler,
                fixedFileHandler
            )
            val uploadFixedMeasurementsService = sDCardUploadFixedMeasurementsServiceFactory.create(
                fixedFileHandler,
                csvLineParameterHandler
            )

            sdCardSyncService = sdCardSyncServiceFactory.create(
                mobileSessionsProcessor,
                fixedSessionsProcessor,
                uploadFixedMeasurementsService,
                csvFileChecker,
                sDCardFileService,
                intent.getStringExtra(UUID_EXTRA_KEY)
            )
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun startSensor(intent: Intent?) {
        intent ?: return

        val deviceItem =
            intent.getParcelableExtra<DeviceItem>(AirBeamRecordSessionService.DEVICE_ITEM_KEY)

        if (deviceItem == null) {
            errorHandler.handle(AirbeamServiceError("DeviceItem passed through intent is null"))
        } else {
            connect(deviceItem)
        }
    }

    override fun onConnectionSuccessful(deviceItem: DeviceItem, sessionUUID: String?) {
        val airBeamConnector = mAirBeamConnector

        // V2 firmware has no SD card — manual sync runs over BLE + WiFi AP via the orchestrator.
        // Short-circuit the V1 SD-card flow when the fallback connector successfully connected
        // over V2; otherwise fall through to the existing SDCardSyncService pipeline.
        val v2Connected = (airBeamConnector as? AirBeamMiniFallbackConnector)?.isV2Connected() == true
        if (v2Connected) {
            ioScope.launch {
                Log.d("AirBeamSyncService", "V2 connection — running manual sync orchestrator")
                val ok = v2StateRepository.startSync()
                Log.d("AirBeamSyncService", "V2 manual sync orchestrator finished ok=$ok")
                airBeamConnector.disconnect()
                EventBus.getDefault().post(SDCardSyncFinished())
                stopSelf()
            }
            return
        }

        sdCardSyncService.start(airBeamConnector, deviceItem)
    }

    /**
     * Defer service teardown while V2 manual sync is in progress. BLE+Wi-Fi coex can drop
     * the BLE link mid-HTTP-download (Android 12 + ESP32); without this guard the inherited
     * `stopSelf()` cancels the orchestrator before it can persist what was already received
     * over the SoftAP. Orchestrator coroutine in [onConnectionSuccessful] calls `stopSelf()`
     * itself when it finishes.
     */
    override fun onDisconnect(deviceId: String) {
        if (v2StateRepository.syncInProgress) {
            Log.d("AirBeamSyncService", "BLE disconnect during V2 sync — deferring stopSelf until orchestrator finishes")
            return
        }
        super.onDisconnect(deviceId)
    }
}
