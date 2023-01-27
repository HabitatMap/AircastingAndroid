package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.onCompletion
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

    fun run(airBeamConnector: AirBeamConnector, deviceItem: DeviceItem) {
        Log.d(TAG, "Downloading measurements from SD card")

        EventBus.getDefault().safeRegister(this)
        mAirBeamConnector = airBeamConnector
        mDeviceItem = deviceItem

        airBeamConnector.triggerSDCardDownload()

        mSDCardFileService.run(
            onLinesDownloaded = { step, linesCount ->
                val event = SDCardLinesReadEvent(step, linesCount)
                EventBus.getDefault().post(event)
            },
            onDownloadFinished = { filePathByMeasurementsCount ->
                checkDownloadedFiles(filePathByMeasurementsCount)
            }
        )
    }

    private fun checkDownloadedFiles(
        filePathByMeasurementsCount: Map<String, Int>
    ) = coroutineScope.launch {
        Log.d(TAG, "Checking downloaded files")

        mSDCardCSVFileChecker.checkFilesForCorruption(filePathByMeasurementsCount)
            .onCompletion {
                syncMobileSessionWithBackendAndFinish()
            }
            .collect { fileToResult ->
                val file = fileToResult.first
                Log.v(TAG, "Consuming file: $file")

                val fileIsCorrupted = !fileToResult.second
                if (fileIsCorrupted) {
                    mSDCardFileService.delete(file)
                    Log.e(TAG, "File $file is corrupted")
                    return@collect
                }

                if (file.isMobile())
                    performAveragingAndSaveMobileMeasurementsLocallyFrom(file)
                else {
                    saveFixedMeasurementsLocallyFrom(file)?.invokeOnCompletion {
                        sendFixedMeasurementsToBackendFrom(file)
                    }
                }
            }
    }

    private fun File.isMobile() = name.contains(SDCardCSVFileFactory.mobileFilesLocation)

    private fun saveFixedMeasurementsLocallyFrom(file: File): Job? {
        val deviceItem = mDeviceItem ?: return null

        Log.d(TAG, "Processing fixed sessions")

        return mSDCardFixedSessionsProcessor.run(file, deviceItem.id)
    }

    private fun handleError(exception: BaseException) {
        EventBus.getDefault().post(SDCardSyncErrorEvent(exception))
    }

    private fun performAveragingAndSaveMobileMeasurementsLocallyFrom(file: File) {
        val deviceItem = mDeviceItem ?: return

        Log.d(TAG, "Processing mobile session from $file")

        mSDCardMobileSessionsProcessor.run(
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
        uploadFixedMeasurementsService.run(
            file,
            deviceItem.id,
        )
    }

    private fun finish() {
        Log.d(TAG, "Clearing SD card")
        mAirBeamConnector?.clearSDCard()

        mSDCardFileService.deleteAllSyncFiles()

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
        coroutineScope.cancel()
    }
}
