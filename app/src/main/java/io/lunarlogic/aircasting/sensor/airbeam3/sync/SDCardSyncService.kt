package io.lunarlogic.aircasting.sensor.airbeam3.sync

import android.util.Log
import io.lunarlogic.aircasting.events.sdcard.SDCardLinesReadEvent
import io.lunarlogic.aircasting.events.sdcard.SDCardSyncErrorEvent
import io.lunarlogic.aircasting.exceptions.*
import io.lunarlogic.aircasting.networking.services.SessionsSyncService
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import io.lunarlogic.aircasting.sensor.AirBeamConnector
import org.greenrobot.eventbus.EventBus

class SDCardSyncService(
    private val mSDCardDownloadService: SDCardDownloadService,
    private val mSDCardCSVFileChecker: SDCardCSVFileChecker,
    private val mSDCardMobileSessionsProcessor: SDCardMobileSessionsProcessor,
    private val mSessionsSyncService: SessionsSyncService?,
    private val mSDCardUploadFixedMeasurementsService: SDCardUploadFixedMeasurementsService?,
    private val mErrorHandler: ErrorHandler
) {
    private val TAG = "SDCardSyncService"

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

        airBeamConnector.triggerSDCardDownload()

        mSDCardDownloadService.run(
            onLinesDownloaded = { step, linesCount ->
                val event = SDCardLinesReadEvent(step, linesCount)
                EventBus.getDefault().post(event)
            },
            onDownloadFinished = { steps -> checkDownloadedFiles(airBeamConnector, deviceItem, steps) }
        )
    }

    private fun checkDownloadedFiles(airBeamConnector: AirBeamConnector, deviceItem: DeviceItem, steps: List<SDCardReader.Step>) {
        Log.d(TAG, "Checking downloaded files")

        if (mSDCardCSVFileChecker.run(steps)) {
            clearSDCard(airBeamConnector)
            saveMobileMeasurementsLocally(airBeamConnector, deviceItem)
        } else {
            // fatal error, we can't proceed with sync
            handleError(SDCardDownloadedFileCorrupted())
        }
    }

    private fun handleError(exception: BaseException) {
        EventBus.getDefault().post(SDCardSyncErrorEvent(exception))
    }

    private fun saveMobileMeasurementsLocally(airBeamConnector: AirBeamConnector, deviceItem: DeviceItem) {
        Log.d(TAG, "Processing mobile sessions")

        mSDCardMobileSessionsProcessor.run(deviceItem.id,
            onFinishCallback = {
                sendMobileMeasurementsToBackend(airBeamConnector, deviceItem)
            }
        )
    }

    private fun sendMobileMeasurementsToBackend(airBeamConnector: AirBeamConnector, deviceItem: DeviceItem) {
        val sessionsSyncService = mSessionsSyncService

        if (sessionsSyncService == null) {
            val cause = SDCardMissingSessionsSyncServiceError()
            mErrorHandler.handleAndDisplay(SDCardSessionsFinalSyncError(cause))
            return
        }

        Log.d(TAG, "Sending mobile sessions to backend")
        sessionsSyncService.sync(
            onSuccessCallback = {
                sendFixedMeasurementsToBackend(airBeamConnector, deviceItem)
            },
            onErrorCallack = {
                mErrorHandler.handleAndDisplay(SDCardSessionsFinalSyncError())
            }
        )
    }

    private fun sendFixedMeasurementsToBackend(airBeamConnector: AirBeamConnector, deviceItem: DeviceItem) {
        val uploadFixedMeasurementsService = mSDCardUploadFixedMeasurementsService

        if (uploadFixedMeasurementsService == null) {
            val cause = SDCardMissingSDCardUploadFixedMeasurementsServiceError()
            mErrorHandler.handleAndDisplay(SDCardSessionsFinalSyncError(cause))
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
        mSDCardDownloadService.deleteFiles()
        Log.d(TAG, "Sync finished")
    }
}
