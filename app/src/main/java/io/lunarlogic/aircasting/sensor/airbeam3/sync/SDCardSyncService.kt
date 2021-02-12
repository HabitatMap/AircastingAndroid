package io.lunarlogic.aircasting.sensor.airbeam3.sync

import io.lunarlogic.aircasting.networking.services.SessionsSyncService
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import io.lunarlogic.aircasting.sensor.AirBeamConnector
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.TimeUnit

class SDCardSyncService(
    private val mSDCardDownloadService: SDCardDownloadService,
    private val mSDCardCSVFileChecker: SDCardCSVFileChecker,
    private val mSDCardMeasurementsCreator: SDCardMeasurementsCreator,
    private val mSessionsSyncService: SessionsSyncService?
) {
    private var mSyncStartedAt: Long? = null // TODO: remove it after implementing proper sync UI

    fun run(airBeamConnector: AirBeamConnector, deviceItem: DeviceItem) {
        mSyncStartedAt = System.currentTimeMillis()

        // TODO: SessionSyncService.sync here or before mSDCardMeasurementsCreator.run
        airBeamConnector.triggerSDCardDownload()

        mSDCardDownloadService.run(
            onLinesDownloaded = { step, linesCount ->
                // TODO: display step type
                showMessage("Syncing $linesCount/${step.measurementsCount}")
            },
            onDownloadFinished = { steps ->
                val isSyncFileCorrect = mSDCardCSVFileChecker.run(steps)
                mSDCardMeasurementsCreator.run(deviceItem.id)
                mSessionsSyncService?.sync()
                // TODO: move it really to the finish
                showFinishMessage(isSyncFileCorrect)
            }
        )

    }

    // TODO: temp thing
    private fun showFinishMessage(isSyncFileCorrect: Boolean) {
        val endedAt = System.currentTimeMillis()
        showMessage("Checking downloaded file...")

        // TODO:
        val downloadedMessage = "Downloaded"// "Downloaded $counter/$count."
        var timeMessage = ""
        mSyncStartedAt?.let { startedAt ->
            val interval = endedAt - startedAt
            timeMessage = "In ${formatDownloadTimeDuration(interval)}."
        }

        val downloadedFileMessage = if (isSyncFileCorrect) {
            "Sync file is correct!"
        } else {
            "Something is wrong with downloaded file :/"
        }

        val message = "Downloading from SD card finished.\n" +
            "$downloadedMessage\n" +
            "$timeMessage\n" +
            downloadedFileMessage

        EventBus.getDefault().post(SyncFinishedEvent(message))
    }

    private fun showMessage(message: String) {
        EventBus.getDefault().post(SyncEvent(message))
    }

    // TODO: temp thing
    private fun formatDownloadTimeDuration(duration: Long): String {
        return String.format("%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(duration),
            TimeUnit.MILLISECONDS.toMinutes(duration) -
                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
            TimeUnit.MILLISECONDS.toSeconds(duration) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }
}
