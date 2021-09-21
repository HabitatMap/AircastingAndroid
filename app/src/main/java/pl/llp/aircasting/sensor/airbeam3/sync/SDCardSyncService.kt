package pl.llp.aircasting.sensor.airbeam3.sync

import android.util.Log
import pl.llp.aircasting.events.sdcard.SDCardLinesReadEvent
import pl.llp.aircasting.events.sdcard.SDCardSyncErrorEvent
import pl.llp.aircasting.events.sessions_sync.SessionsSyncErrorEvent
import pl.llp.aircasting.events.sessions_sync.SessionsSyncSuccessEvent
import pl.llp.aircasting.exceptions.*
import pl.llp.aircasting.lib.safeRegister
import pl.llp.aircasting.networking.services.SessionsSyncService
import pl.llp.aircasting.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.sensor.AirBeamConnector
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.events.sdcard.SDCardClearFinished
import pl.llp.aircasting.events.sdcard.SDCardSyncFinished
import pl.llp.aircasting.networking.services.AverageAndSyncSDCardSessionsService
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.timerTask

class SDCardSyncService(
    private val mSDCardDownloadService: SDCardDownloadService,
    private val mSDCardCSVFileChecker: SDCardCSVFileChecker,
    private val mSDCardMobileSessionsProcessor: SDCardMobileSessionsProcessor,
    private val mSessionsSyncService: SessionsSyncService?,
    private val mSDCardUploadFixedMeasurementsService: SDCardUploadFixedMeasurementsService?,
    private val mErrorHandler: ErrorHandler
) {
    private val TAG = "SDCardSyncService"

    private var mAirBeamConnector: AirBeamConnector? = null
    private var mDeviceItem: DeviceItem? = null

    private var mSessionsSyncStarted = AtomicBoolean(false)
    /*

        High level sync flow:

        1. Refresh sessions list using SessionsSyncService so already deleted on the backend got deleted and so on (done in SyncController#refreshSessionList)
        2. Download measurements from AirBeam3 SD card to files/sync/mobile.csv and files/sync/fixed.csv
        3. Check downloaded files (checks if downloaded file has at least 80% of expected lines and if there is at most 20% of corrupted lines)
        4. Save mobile measurements for disconnected sessions in the Android local db. Create sessions named "Imported from SD card" for every UUID that doesn't match with existing session.
        5. Send mobile measurements to the backend using SessionsSyncService.
        6. Send fixed measurements to the backend - in this case backend knows more than Android app, so we are sending all of them and backend decides what to do with them.

     */

    fun run(airBeamConnector: AirBeamConnector, deviceItem: DeviceItem) {
        Log.d(TAG, "Downloading measurements from SD card")

        EventBus.getDefault().safeRegister(this)
        mAirBeamConnector = airBeamConnector
        mDeviceItem = deviceItem

        airBeamConnector.triggerSDCardDownload()
        mErrorHandler.handle(SDCardSyncError(" run() after  triggerSDCardDownload"))

        mSDCardDownloadService.run(
            onLinesDownloaded = { step, linesCount ->
                val event = SDCardLinesReadEvent(step, linesCount)
                EventBus.getDefault().post(event)
            },
            onDownloadFinished = { steps -> checkDownloadedFiles(steps) }
        )
    }

    private fun checkDownloadedFiles(steps: List<SDCardReader.Step>) {
        mErrorHandler.handle(SDCardSyncError(" onDownloadFinished, checkDownloadedFiles,  mAirBeamConnector ${mAirBeamConnector}"))
        val airBeamConnector = mAirBeamConnector ?: return

        Log.d(TAG, "Checking downloaded files")

        if (mSDCardCSVFileChecker.run(steps)) {
            clearSDCard(airBeamConnector)
            saveMobileMeasurementsLocally()
        } else {
            // fatal error, we can't proceed with sync
            handleError(SDCardDownloadedFileCorrupted())
            cleanup()
        }
    }

    private fun handleError(exception: BaseException) {
        EventBus.getDefault().post(SDCardSyncErrorEvent(exception))
    }

    private fun saveMobileMeasurementsLocally() {
        mErrorHandler.handle(SDCardSyncError("saveMobileMeasurementsLocally, mDeviceItem ${mDeviceItem}"))
        val deviceItem = mDeviceItem ?: return

        Log.d(TAG, "Processing mobile sessions")
        mErrorHandler.handle(SDCardSyncError("Processing mobile sessions"))

        mSDCardMobileSessionsProcessor.run(deviceItem.id,
            onFinishCallback = { processedSessionsIds ->
                sendMobileMeasurementsToBackend(processedSessionsIds)
            }
        )
    }

    private fun sendMobileMeasurementsToBackend(sessionsIds: MutableList<Long>) {
        mErrorHandler.handle(SDCardSyncError("sendMobileMeasurementsToBackend"))
        val sessionsSyncService = mSessionsSyncService


        if (sessionsSyncService == null) {
            val cause = SDCardMissingSessionsSyncServiceError()
            mErrorHandler.handleAndDisplay(SDCardSessionsFinalSyncError(cause))
            cleanup()
            return
        }

        Log.d(TAG, "Sending mobile sessions to backend")
        mSessionsSyncStarted.set(true)

        val averageAndSyncSDCardSessionsService = AverageAndSyncSDCardSessionsService(sessionsSyncService, sessionsIds)
        averageAndSyncSDCardSessionsService.start()

        // We leave this screen hanging for 2 sec so the user knows the sync upload was initiated
        Thread.sleep(2000)
        mErrorHandler.handle(SDCardSyncError("sending SessionsSyncSuccessEvent"))
        EventBus.getDefault().post(SessionsSyncSuccessEvent())
    }

    @Subscribe
    fun onMessageEvent(event: SessionsSyncSuccessEvent) {
        mErrorHandler.handle(SDCardSyncError(" onMessageEvent(event: SessionsSyncSuccessEvent) mSessionsSyncStarted.get() ${mSessionsSyncStarted.get()} "))
        if (mSessionsSyncStarted.get()) {
            mSessionsSyncStarted.set(false)
            sendFixedMeasurementsToBackend()
            mErrorHandler.handle(SDCardSyncError(" scheduling posting finishing event after 20 seconds} "))
            val timerTask = timerTask {
                EventBus.getDefault().post(SDCardSyncFinished())
            }
            Timer().schedule(timerTask, 20000)
        }
    }

    @Subscribe
    fun onMessageEvent(event: SessionsSyncErrorEvent) {
        mErrorHandler.handleAndDisplay(SDCardSessionsFinalSyncError())
        cleanup()
    }

    private fun sendFixedMeasurementsToBackend() {
        mErrorHandler.handle(SDCardSyncError("sendFixedMeasurementsToBackend, mDeviceItem ${mDeviceItem}"))
        val deviceItem = mDeviceItem ?: return

        val uploadFixedMeasurementsService = mSDCardUploadFixedMeasurementsService

        if (uploadFixedMeasurementsService == null) {
            val cause = SDCardMissingSDCardUploadFixedMeasurementsServiceError()
            mErrorHandler.handleAndDisplay(SDCardSessionsFinalSyncError(cause))
            cleanup()
            return
        }

        Log.d(TAG, "Sending fixed measurements to backend")
        mErrorHandler.handle(SDCardSyncError("uploadFixedMeasurementsService.run"))
        uploadFixedMeasurementsService.run(deviceItem.id,
            onFinishCallback = { finish() }
        )
    }

    private fun clearSDCard(airBeamConnector: AirBeamConnector) {
        Log.d(TAG, "Clearing SD card")
        mErrorHandler.handle(SDCardSyncError("clearSDCard"))
        airBeamConnector.clearSDCard()
    }

    private fun finish() {
        mErrorHandler.handle(SDCardSyncError("finish()"))
        mSDCardDownloadService.deleteFiles()
        mDeviceItem?.let { deviceItem ->
            mAirBeamConnector?.onDisconnected(deviceItem)
            mAirBeamConnector?.disconnect()
        }

        cleanup()
        Log.d(TAG, "Sync finished")
    }

    private fun cleanup() {
        mErrorHandler.handle(SDCardSyncError("cleanup()"))
        EventBus.getDefault().unregister(this)
        mDeviceItem = null
        mAirBeamConnector = null
    }
}
