package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.CoroutineContextProviderImpl
import pl.llp.aircasting.util.events.SessionsSyncErrorEvent
import pl.llp.aircasting.util.events.SessionsSyncSuccessEvent
import pl.llp.aircasting.util.events.sdcard.SDCardLinesReadEvent
import pl.llp.aircasting.util.events.sdcard.SDCardSyncErrorEvent
import pl.llp.aircasting.util.events.sdcard.SDCardSyncFinished
import pl.llp.aircasting.util.exceptions.*
import pl.llp.aircasting.util.extensions.safeRegister
import pl.llp.aircasting.util.helpers.sensor.AirBeamConnector
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class SDCardSyncService(
    private val mSDCardFileService: SDCardFileService,
    private val mSDCardCSVFileChecker: SDCardCSVFileChecker,
    private val mSDCardMobileSessionsProcessor: SDCardMobileSessionsProcessor,
    private val mSDCardFixedSessionsProcessor: SDCardFixedSessionsProcessor,
    private val mSessionsSyncService: SessionsSyncService?,
    private val mSDCardUploadFixedMeasurementsService: SDCardUploadFixedMeasurementsService?,
    private val mErrorHandler: ErrorHandler,
    private val coroutineScope: CoroutineScope = CoroutineScope(
        CoroutineContextProviderImpl(
            Dispatchers.IO
        ).context()
    )
) {
    private val TAG = "SDCardSyncService"

    private var mAirBeamConnector: AirBeamConnector? = null
    private var mDeviceItem: DeviceItem? = null

    private var mSessionsSyncStartedByThis = AtomicBoolean(false)
    /*

        High level sync flow:

        1. Refresh sessions list using SessionsSyncService so already deleted on the backend got deleted and so on (done in SyncController#refreshSessionList)
        2. Download measurements from AirBeam3 SD card to files/sync/mobile.csv and files/sync/fixed.csv
        3. Check downloaded files (checks if downloaded file has at least 80% of expected lines and if there is at most 20% of corrupted lines)
        4. Save mobile measurements for disconnected sessions in the Android local db. Create sessions named "Imported from SD card" for every UUID that doesn't match with existing session.
        5. Send mobile measurements to the backend using SessionsSyncService.
        6. Save filtered fixed measurements in the Android local db.
        7. Send fixed measurements to the backend

     */

    fun start(airBeamConnector: AirBeamConnector, deviceItem: DeviceItem) {
        Log.d(TAG, "Downloading measurements from SD card")

        EventBus.getDefault().safeRegister(this)
        mAirBeamConnector = airBeamConnector
        mDeviceItem = deviceItem

        airBeamConnector.triggerSDCardDownload()

        mSDCardFileService.start(
            onLinesDownloaded = { step, linesCount ->
                val event = SDCardLinesReadEvent(step, linesCount)
                EventBus.getDefault().post(event)
            },
            onDownloadFinished = { stepsByFilePaths ->
                handleDownloadedFiles(stepsByFilePaths)
            }
        )
    }

    private fun handleDownloadedFiles(
        stepsByFilePaths: Map<SDCardReader.Step?, List<String>>
    ) = coroutineScope.launch {
        Log.d(TAG, "Checking downloaded files")

        if (mSDCardCSVFileChecker.areFilesCorrupted(stepsByFilePaths)) {
            terminateSync()
        } else {
            clearSDCard()
            saveMeasurements(stepsByFilePaths).invokeOnCompletion {
                syncMobileSessionWithBackendAndFinish()
            }
        }
    }

    private fun clearSDCard() {
        Log.d(TAG, "Clearing SD card")
        mAirBeamConnector?.clearSDCard()
    }

    private fun CoroutineScope.saveMeasurements(
        stepsByFilePaths: Map<SDCardReader.Step?, List<String>>
    ) = launch {
        Log.v(TAG, "Saving measurements locally")
        stepsByFilePaths.entries.forEach { entry ->
            Log.v(TAG, "Current stepByFilePath entry: $entry")
            when (entry.key?.type) {
                SDCardReader.StepType.MOBILE -> launch {
                    entry.value.forEach { path ->
                        performAveragingAndSaveMobileMeasurementsLocallyFrom(File(path))
                    }
                }
                SDCardReader.StepType.FIXED_CELLULAR, SDCardReader.StepType.FIXED_WIFI -> launch {
                    entry.value.forEach { path ->
                        val file = File(path)
                        saveFixedMeasurementsLocallyFrom(file)
                        sendFixedMeasurementsToBackendFrom(file)
                    }
                }
                else -> terminateSync()
            }
        }
    }

    private fun terminateSync() {
        handleError(SDCardDownloadedFileCorrupted())
        cleanup()
    }

    private fun saveFixedMeasurementsLocallyFrom(file: File): Job? {
        val deviceItem = mDeviceItem ?: return null

        Log.d(TAG, "Processing fixed sessions")

        return mSDCardFixedSessionsProcessor.start(file, deviceItem.id)
    }

    private fun handleError(exception: BaseException) {
        EventBus.getDefault().post(SDCardSyncErrorEvent(exception))
    }

    private fun performAveragingAndSaveMobileMeasurementsLocallyFrom(file: File) {
        val deviceItem = mDeviceItem ?: return

        Log.d(TAG, "Processing mobile session from $file")

        mSDCardMobileSessionsProcessor.start(
            file,
            deviceItem.id,
        )
    }

    private fun syncMobileSessionWithBackendAndFinish() {
        val sessionsSyncService = mSessionsSyncService

        if (sessionsSyncService == null) {
            val cause = SDCardMissingSessionsSyncServiceError()
            mErrorHandler.handleAndDisplay(SDCardSessionsFinalSyncError(cause))
            cleanup()
            return
        }

        Log.d(TAG, "Syncing mobile sessions with backend")
        // TODO: Refactor sessions sync service to use suspend call
        mSessionsSyncStartedByThis.set(true)
        sessionsSyncService.sync()
    }

    @Subscribe
    fun onMessageEvent(event: SessionsSyncSuccessEvent) {
        if (mSessionsSyncStartedByThis.get()) {
            mSessionsSyncStartedByThis.set(false)
            finish()
        }
    }

    @Subscribe
    fun onMessageEvent(event: SessionsSyncErrorEvent) {
        mErrorHandler.handleAndDisplay(SDCardSessionsFinalSyncError())
        cleanup()
    }

    private fun sendFixedMeasurementsToBackendFrom(file: File) {
        val deviceItem = mDeviceItem ?: return

        val uploadFixedMeasurementsService = mSDCardUploadFixedMeasurementsService

        if (uploadFixedMeasurementsService == null) {
            val cause = SDCardMissingSDCardUploadFixedMeasurementsServiceError()
            mErrorHandler.handleAndDisplay(SDCardSessionsFinalSyncError(cause))
            cleanup()
            return
        }

        Log.d(TAG, "Sending fixed measurements to backend")
        uploadFixedMeasurementsService.start(
            file,
            deviceItem.id,
        )
    }

    private fun finish() {
        mSDCardFileService.deleteAllSyncFiles()
        Log.d(TAG, "Sync finishing")
        mDeviceItem?.let { deviceItem ->
            mAirBeamConnector?.onDisconnected(deviceItem, false)
            mAirBeamConnector?.disconnect()
        }

        cleanup()
        mErrorHandler.handle(SDCardSyncError("finish(), posting SDCardSyncFinished"))
        EventBus.getDefault().post(SDCardSyncFinished())
        Log.d(TAG, "Sync finished")
    }

    private fun cleanup() {
        EventBus.getDefault().unregister(this)
        mDeviceItem = null
        mAirBeamConnector = null
    }
}
