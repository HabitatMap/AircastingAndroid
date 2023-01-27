package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.data.api.services.AverageAndSyncSDCardSessionsService
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

    private var mSessionsSyncStarted = AtomicBoolean(false)
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
                // clear SD card?
            }
            .collect { fileToResult ->
                val file = fileToResult.first

                val fileIsCorrupted = !fileToResult.second
                if (fileIsCorrupted) {
                    mSDCardFileService.delete(file)
                    Log.e(TAG, "File $file is corrupted")
                    return@collect
                }

                if (file.isMobile())
                    performAveragingAndSaveMeasurements(file)
                else
                    saveFixedMeasurementsLocally(file)
            }

//            if (mSDCardCSVFileChecker.checkFilesForCorruption(filePathByMeasurementsCount)) {
//                clearSDCard(airBeamConnector)
//                performAveragingAndSaveMeasurements()
//            } else {
//                // fatal error, we can't proceed with sync
//                handleError(SDCardDownloadedFileCorrupted())
//                cleanup()
//            }
    }

    private fun File.isMobile() = name.contains(SDCardCSVFileFactory.mobileFilesLocation)

    private fun saveFixedMeasurementsLocally(file: File) {
        val deviceItem = mDeviceItem ?: return

        Log.d(TAG, "Processing fixed sessions")

        mSDCardFixedSessionsProcessor.run(file, deviceItem.id)
    }

    private fun handleError(exception: BaseException) {
        EventBus.getDefault().post(SDCardSyncErrorEvent(exception))
    }

    private fun performAveragingAndSaveMeasurements(file: File) {
        val deviceItem = mDeviceItem ?: return

        Log.d(TAG, "Processing mobile sessions")

        mSDCardMobileSessionsProcessor.run(
            file,
            deviceItem.id,
        ) { processedSessionsIds ->
            sendMobileMeasurementsToBackend(processedSessionsIds)
        }
    }

    private fun sendMobileMeasurementsToBackend(sessionsIds: MutableList<Long>) {
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
    }

    @Subscribe
    fun onMessageEvent(event: SessionsSyncSuccessEvent) {
        if (mSessionsSyncStarted.get()) {
            mSessionsSyncStarted.set(false)
            sendFixedMeasurementsToBackend()
        }
    }

    @Subscribe
    fun onMessageEvent(event: SessionsSyncErrorEvent) {
        mErrorHandler.handleAndDisplay(SDCardSessionsFinalSyncError())
        cleanup()
    }

    private fun sendFixedMeasurementsToBackend() {
        val deviceItem = mDeviceItem ?: return

        val uploadFixedMeasurementsService = mSDCardUploadFixedMeasurementsService

        if (uploadFixedMeasurementsService == null) {
            val cause = SDCardMissingSDCardUploadFixedMeasurementsServiceError()
            mErrorHandler.handleAndDisplay(SDCardSessionsFinalSyncError(cause))
            cleanup()
            return
        }

        Log.d(TAG, "Sending fixed measurements to backend")
        uploadFixedMeasurementsService.run(deviceItem.id,
            onFinishCallback = { finish() }
        )
    }

    private fun clearSDCard(airBeamConnector: AirBeamConnector) {
        Log.d(TAG, "Clearing SD card")
        airBeamConnector.clearSDCard()
    }

    private fun finish() {
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
