package io.lunarlogic.aircasting.sensor.airbeam3.sync

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
    private val mErrorHandler: ErrorHandler
) {
    fun run(airBeamConnector: AirBeamConnector, deviceItem: DeviceItem) {
        val sessionsSyncService = mSessionsSyncService

        if (sessionsSyncService == null) {
            val cause = SDCardMissingSessionsSyncServiceError()
            mErrorHandler.handleAndDisplay(SDCardSessionsInitialSyncError(cause))
            return
        }

        sessionsSyncService.sync(
            onSuccessCallback = { performSDCardDownload(airBeamConnector, deviceItem)},
            onErrorCallack = { mErrorHandler.handleAndDisplay(SDCardSessionsInitialSyncError()) }
        )
    }

    private fun performSDCardDownload(airBeamConnector: AirBeamConnector, deviceItem: DeviceItem) {
        airBeamConnector.triggerSDCardDownload()

        mSDCardDownloadService.run(
            onLinesDownloaded = { step, linesCount ->
                // TODO: display step type?
                showMessage("Syncing $linesCount/${step.measurementsCount}")
            },
            onDownloadFinished = { steps -> checkDownloadedFile(airBeamConnector, deviceItem, steps) }
        )
    }

    private fun checkDownloadedFile(airBeamConnector: AirBeamConnector, deviceItem: DeviceItem, steps: List<SDCardReader.Step>) {
        if (mSDCardCSVFileChecker.run(steps)) {
            processMeasurements(airBeamConnector, deviceItem)
        } else {
            mErrorHandler.handleAndDisplay(SDCardDownloadedFileCorrupted())
        }
    }

    private fun processMeasurements(airBeamConnector: AirBeamConnector, deviceItem: DeviceItem) {
        mSDCardMobileSessionsProcessor.run(deviceItem.id,
            onFinishCallback = {
                sendMeasurementsToBackend(airBeamConnector)
            }
        )
    }

    private fun sendMeasurementsToBackend(airBeamConnector: AirBeamConnector) {
        val sessionsSyncService = mSessionsSyncService

        if (sessionsSyncService == null) {
            val cause = SDCardMissingSessionsSyncServiceError()
            mErrorHandler.handleAndDisplay(SDCardSessionsFinalSyncError(cause))
            return
        }

        sessionsSyncService.sync(
            onSuccessCallback = {
                clearSDCard(airBeamConnector)
            },
            onErrorCallack = {
                mErrorHandler.handleAndDisplay(SDCardSessionsFinalSyncError())
            }
        )
    }

    private fun clearSDCard(airBeamConnector: AirBeamConnector) {
        // TODO: should we check something before clearing?
        airBeamConnector.clearSDCard()
    }

    private fun showMessage(message: String) {
        EventBus.getDefault().post(SyncEvent(message))
    }
}
