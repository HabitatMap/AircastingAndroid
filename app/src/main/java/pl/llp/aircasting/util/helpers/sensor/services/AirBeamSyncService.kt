package pl.llp.aircasting.util.helpers.sensor.services

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.di.modules.IoCoroutineScope
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.events.V2WifiPickerEducationConfirmedEvent
import pl.llp.aircasting.util.events.V2WifiPickerEducationRequestedEvent
import pl.llp.aircasting.util.events.sdcard.SDCardSyncErrorEvent
import pl.llp.aircasting.util.events.sdcard.SDCardSyncFinished
import pl.llp.aircasting.util.extensions.safeRegister
import pl.llp.aircasting.util.exceptions.AirbeamServiceError
import pl.llp.aircasting.util.exceptions.V2ManualSyncError
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

    @Inject
    lateinit var sessionsRepository: pl.llp.aircasting.data.local.repository.SessionsRepository

    @field:[Inject IoCoroutineScope]
    lateinit var ioScope: CoroutineScope

    private lateinit var sdCardSyncService: SDCardSyncService

    /**
     * Set while the V2 orchestrator is suspended on the educational dialog. The
     * `AirbeamSyncingController` posts [V2WifiPickerEducationConfirmedEvent] when the user
     * presses Continue; `onMessageEvent` below completes this deferred so the orchestrator
     * resumes and the system Wi-Fi picker is launched.
     */
    private var pendingPickerEducation: CompletableDeferred<Unit>? = null
    private var syncConfirmationDeferred: CompletableDeferred<Boolean>? = null

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
            EventBus.getDefault().safeRegister(this)
            ioScope.launch {
                Log.d("AirBeamSyncService", "V2 connection — running manual sync orchestrator")

                val fileSize = v2StateRepository.savedSessionFileSize
                val uuid = v2StateRepository.savedSessionUuid
                val session = if (!uuid.isNullOrEmpty()) {
                    sessionsRepository.getSessionByUUID(uuid)
                } else {
                    null
                }
                val estimatedSeconds = pl.llp.aircasting.ui.view.screens.sync.syncing.AirbeamSyncingController.calculateEstimatedSyncTimeSeconds(fileSize, session)

                val confirmed = awaitSyncConfirmation(estimatedSeconds)
                if (!confirmed) {
                    Log.d("AirBeamSyncService", "Sync cancelled by user")
                    runCatching { EventBus.getDefault().unregister(this@AirBeamSyncService) }
                    airBeamConnector.disconnect()
                    stopSelf()
                    return@launch
                }

                val ok = runCatching {
                    v2StateRepository.startSync(onBeforePicker = ::awaitWifiPickerEducation)
                }.getOrElse { e ->
                    Log.e("AirBeamSyncService", "V2 manual sync orchestrator threw", e)
                    false
                }
                Log.d("AirBeamSyncService", "V2 manual sync orchestrator finished ok=$ok")
                runCatching { EventBus.getDefault().unregister(this@AirBeamSyncService) }
                airBeamConnector.disconnect()
                if (ok) {
                    EventBus.getDefault().post(SDCardSyncFinished(isV2 = true))
                } else {
                    EventBus.getDefault().post(SDCardSyncErrorEvent(V2ManualSyncError()))
                }
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

    private suspend fun awaitWifiPickerEducation() {
        val deferred = CompletableDeferred<Unit>()
        pendingPickerEducation = deferred
        EventBus.getDefault().post(V2WifiPickerEducationRequestedEvent())
        try {
            deferred.await()
        } finally {
            pendingPickerEducation = null
        }
    }

    private suspend fun awaitSyncConfirmation(estimatedSeconds: Long): Boolean {
        val deferred = CompletableDeferred<Boolean>()
        syncConfirmationDeferred = deferred
        EventBus.getDefault().post(pl.llp.aircasting.util.events.sdcard.V2SyncConfirmationRequestedEvent(estimatedSeconds))
        return try {
            deferred.await()
        } finally {
            syncConfirmationDeferred = null
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: pl.llp.aircasting.util.events.sdcard.V2SyncConfirmationResponseEvent) {
        syncConfirmationDeferred?.complete(event.confirmed)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: V2WifiPickerEducationConfirmedEvent) {
        pendingPickerEducation?.complete(Unit)
    }
}
