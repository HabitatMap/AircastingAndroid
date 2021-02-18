package io.lunarlogic.aircasting.sensor.airbeam3.sync

import android.util.Log
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

    // TODO: move deviceId to the file name?
    fun run(airBeamConnector: AirBeamConnector, deviceItem: DeviceItem) {
        val sessionsSyncService = mSessionsSyncService

        if (sessionsSyncService == null) {
            val cause = SDCardMissingSessionsSyncServiceError()
            mErrorHandler.handleAndDisplay(SDCardSessionsInitialSyncError(cause))
            return
        }

        Log.d(TAG, "Initial sync to refresh session list")
        sessionsSyncService.sync(
            onSuccessCallback = { performSDCardDownload(airBeamConnector, deviceItem)},
            onErrorCallack = { mErrorHandler.handleAndDisplay(SDCardSessionsInitialSyncError()) }
        )
    }

    private fun performSDCardDownload(airBeamConnector: AirBeamConnector, deviceItem: DeviceItem) {
        Log.d(TAG, "Downloading measurements from SD card")

        airBeamConnector.triggerSDCardDownload()

        mSDCardDownloadService.run(
            onLinesDownloaded = { step, linesCount ->
                showMessage("Syncing $linesCount/${step.measurementsCount}")
            },
            onDownloadFinished = { steps -> checkDownloadedFile(airBeamConnector, deviceItem, steps) }
        )
    }

    private fun checkDownloadedFile(airBeamConnector: AirBeamConnector, deviceItem: DeviceItem, steps: List<SDCardReader.Step>) {
        Log.d(TAG, "Checking downloaded files")

        if (mSDCardCSVFileChecker.run(steps)) {
            processMobileMeasurements(airBeamConnector, deviceItem)
        } else {
            mErrorHandler.handleAndDisplay(SDCardDownloadedFileCorrupted())
        }
    }

    private fun processMobileMeasurements(airBeamConnector: AirBeamConnector, deviceItem: DeviceItem) {
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
            onFinishCallback = { clearSDCard(airBeamConnector) }
        )
    }

    private fun clearSDCard(airBeamConnector: AirBeamConnector) {
        Log.d(TAG, "Clearing SD card")
        airBeamConnector.clearSDCard()
    }

    private fun showMessage(message: String) {
        EventBus.getDefault().post(SyncEvent(message))
    }
}
