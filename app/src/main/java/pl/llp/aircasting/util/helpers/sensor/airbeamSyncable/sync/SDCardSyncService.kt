package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync

import android.util.Log
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.BuildConfig
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.di.modules.IoCoroutineScope
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.events.sdcard.SDCardSyncErrorEvent
import pl.llp.aircasting.util.events.sdcard.SDCardSyncFinished
import pl.llp.aircasting.util.exceptions.BaseException
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.SDCardDownloadedFileCorrupted
import pl.llp.aircasting.util.exceptions.SDCardMissingSDCardUploadFixedMeasurementsServiceError
import pl.llp.aircasting.util.exceptions.SDCardSessionsFinalSyncError
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileChecker.SDCardCSVFileChecker
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileService.SDCardFileService
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.sessionProcessor.SDCardFixedSessionsProcessor
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.sessionProcessor.SDCardMobileSessionsProcessor
import pl.llp.aircasting.util.helpers.sensor.common.SessionFinisher
import pl.llp.aircasting.util.helpers.sensor.common.connector.AirBeamConnector
import pl.llp.aircasting.util.sdSyncFinishedCountingIdleResource
import java.io.File

@AssistedFactory
interface SDCardSyncServiceFactory {
    fun create(
        sDCardMobileSessionsProcessor: SDCardMobileSessionsProcessor,
        sDCardFixedSessionsProcessor: SDCardFixedSessionsProcessor,
        mSDCardUploadFixedMeasurementsService: SDCardUploadFixedMeasurementsService?,
        mSDCardCSVFileChecker: SDCardCSVFileChecker,
        mSDCardFileService: SDCardFileService,
        sessionUuid: String?,
    ): SDCardSyncService
}

class SDCardSyncService @AssistedInject constructor(
    private val mSessionsSyncService: SessionsSyncService,
    private val mErrorHandler: ErrorHandler,
    @IoCoroutineScope private val coroutineScope: CoroutineScope,
    private val finishSession: SessionFinisher,
    @Assisted private val mSDCardFileService: SDCardFileService,
    @Assisted private val mSDCardCSVFileChecker: SDCardCSVFileChecker,
    @Assisted private val mSDCardMobileSessionsProcessor: SDCardMobileSessionsProcessor,
    @Assisted private val mSDCardFixedSessionsProcessor: SDCardFixedSessionsProcessor,
    @Assisted private val mSDCardUploadFixedMeasurementsService: SDCardUploadFixedMeasurementsService?,
    @Assisted private val disconnectedSessionUuid: String?,
) {
    private val TAG = "SDCardSyncService"

    private var mAirBeamConnector: AirBeamConnector? = null
    private var mDeviceItem: DeviceItem? = null

    /*
        High level sync flow:

        1. Refresh sessions list using SessionsSyncService so already deleted on the backend got deleted and so on (done in SyncController#refreshSessionList)
        2. Download measurements from AirBeam3 SD card and save each session's measurements in
            separate corresponding files in directories: files/sync/mobile<sessionUuid>.csv and files/sync/fixed<sessionUuid>.csv
        3. Check downloaded files per step (MOBILE, FIXED, FIXED_CELLULAR)
            If downloaded files in one step have more than 20% of lines corrupted - interrupt the sync with error

        *** Next steps are done for each session file:
        MOBILE SESSION FILE:
        4. If measurements require averaging, signal SyncService to average measurements present in local DB,
            and average measurements in the file on the go, while reading them as strings
        5. Save mobile measurements for the disconnected session in the Android local db.
            Create session named "Imported from SD card" for UUID that doesn't match with existing session.

        FIXED SESSION FILE:
        4. Save filtered fixed measurements in the Android local db.
        5. Send fixed measurements from the file to the backend.
        ***

        6. After iterating through all the files, sync mobile measurements with backend.
     */

    fun start(airBeamConnector: AirBeamConnector, deviceItem: DeviceItem) {
        Log.d(TAG, "Downloading measurements from SD card")
        if (BuildConfig.DEBUG) sdSyncFinishedCountingIdleResource.increment()

        mAirBeamConnector = airBeamConnector
        mDeviceItem = deviceItem

        mSDCardFileService.setup { stepsByFilePaths ->
            handleDownloadedFiles(stepsByFilePaths)
        }

        airBeamConnector.triggerSDCardDownload()
    }

    private fun handleDownloadedFiles(
        stepsByFilePaths: Map<SDCardReader.Step?, List<String>>
    ) = coroutineScope.launch {
        Log.d(TAG, "Checking downloaded files")

        if (mSDCardCSVFileChecker.areFilesCorrupted(stepsByFilePaths)) {
            terminateSync()
        } else {
            processSessionsMeasurementsFiles(stepsByFilePaths)
            syncMobileSessionWithBackend()
                .onSuccess { syncResult ->
                    when (syncResult) {
                        is SessionsSyncService.Result.Success -> clearSdCardAndFinish()
                        is SessionsSyncService.Result.Error -> {
                            mErrorHandler.handleAndDisplay(SDCardSessionsFinalSyncError())
                            cleanup()
                        }
                    }
                }
                .onFailure {
                    mErrorHandler.handleAndDisplay(SDCardSessionsFinalSyncError())
                    cleanup()
                }
        }
        if (BuildConfig.DEBUG) sdSyncFinishedCountingIdleResource.decrement()
    }

    private suspend fun processSessionsMeasurementsFiles(
        stepsByFilePaths: Map<SDCardReader.Step?, List<String>>
    ) {
        Log.v(TAG, "Saving measurements locally")
        stepsByFilePaths.entries.forEach { entry ->
            when (entry.key?.type) {
                SDCardReader.StepType.MOBILE -> {
                    entry.value.forEach { path ->
                        performAveragingAndSaveMobileMeasurementsLocallyFrom(File(path))
                    }
                    disconnectedSessionUuid?.let { finishSession(it) }
                }

                SDCardReader.StepType.FIXED_CELLULAR, SDCardReader.StepType.FIXED_WIFI ->
                    entry.value.forEach { path ->
                        val file = File(path)
                        saveFixedMeasurementsLocallyFrom(file)
                        sendFixedMeasurementsToBackendFrom(file)
                    }
            }
        }
    }

    private fun terminateSync() {
        handleError(SDCardDownloadedFileCorrupted())
        cleanup()
    }

    private suspend fun saveFixedMeasurementsLocallyFrom(file: File) {
        val deviceItem = mDeviceItem ?: return

        Log.d(TAG, "Processing fixed sessions")

        return mSDCardFixedSessionsProcessor.start(file, deviceItem.id)
    }

    private fun handleError(exception: BaseException) {
        EventBus.getDefault().post(SDCardSyncErrorEvent(exception))
    }

    private suspend fun performAveragingAndSaveMobileMeasurementsLocallyFrom(file: File) {
        val deviceItem = mDeviceItem ?: return

        Log.d(TAG, "Processing mobile session from $file")

        mSDCardMobileSessionsProcessor.start(
            file,
            deviceItem.id,
        )
    }

    private suspend fun syncMobileSessionWithBackend(): Result<SessionsSyncService.Result> {
        Log.d(TAG, "Syncing mobile sessions with backend")
        return mSessionsSyncService.sync()
    }

    private suspend fun sendFixedMeasurementsToBackendFrom(file: File) {
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

    private suspend fun clearSdCardAndFinish() {
        mSDCardFileService.deleteAllSyncFiles()

        clearSDCard()

        mDeviceItem?.let { deviceItem ->
            mAirBeamConnector?.onDisconnected(deviceItem, false)
            mAirBeamConnector?.disconnect()
        }

        cleanup()
        EventBus.getDefault().post(SDCardSyncFinished())
        Log.d(TAG, "Sync finished")
    }

    private suspend fun clearSDCard() {
        Log.d(TAG, "Clearing SD card")
        mAirBeamConnector?.clearSDCard()
    }

    private fun cleanup() {
        EventBus.getDefault().unregister(this)
        mDeviceItem = null
        mAirBeamConnector = null
    }
}
