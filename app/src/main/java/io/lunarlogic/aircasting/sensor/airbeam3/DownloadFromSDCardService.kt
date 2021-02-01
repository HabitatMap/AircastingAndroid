package io.lunarlogic.aircasting.sensor.airbeam3

import android.content.Context
import android.util.Log
import io.lunarlogic.aircasting.sensor.SyncFileChecker
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileWriter
import java.util.concurrent.TimeUnit

class DownloadFromSDCardService(
    private val mContext: Context
) {
    private val DOWNLOAD_FINISHED = "SD_SYNC_FINISH"
    private val DOWNLOAD_TAG = "SYNC"
    private val CLEAR_FINISHED = "SD_DELETE_FINISH"

    private var fileWriter: FileWriter? = null
    private var count = 0
    private var counter = 0

    private var step = 0 // TOOD: remove it after implementing proper sync
    private var bleCount = 0 // TOOD: remove it after implementing proper sync
    private var wifiCount = 0 // TOOD: remove it after implementing proper sync
    private var cellularCount = 0 // TOOD: remove it after implementing proper sync
    private var downloadStartedAt: Long? = null // TOOD: remove it after implementing proper sync
    class SyncEvent(val message: String) // TOOD: remove it after implementing proper sync
    class SyncFinishedEvent(val message: String) // TOOD: remove it after implementing proper sync


    fun init() {
        count = 0
        downloadStartedAt = System.currentTimeMillis()
        openSyncFile()
    }

    fun onMetaDataDownloaded(data: ByteArray?) {
        data ?: return
        val valueString = String(data)

        try {
            val partialCountString = valueString.split(":").lastOrNull()?.trim()
            val partialCount = partialCountString?.toInt()

            partialCount?.let {
                step += 1
                when(step) {
                    1 -> bleCount = partialCount
                    2 -> wifiCount = partialCount
                    3 -> cellularCount = partialCount
                }
                count += partialCount
            }
        } catch (e: NumberFormatException) {
            // ignore - this is e.g. SD_SYNC_FINISH
        }

        if (valueString == DOWNLOAD_FINISHED) {
            Log.d(DOWNLOAD_TAG, "Sync finished")
            closeSyncFile()
            checkOutputFileAndShowFinishMessage()
        } else if (valueString == CLEAR_FINISHED) {
            showMessage("SD card successfully cleared.")
        }
    }

    fun onMeasurementsDownloaded(data: ByteArray?) {
        data ?: return

        val lines = String(data)
        writeToSyncFile(lines)

        val linesCount = lines.lines().filter { line -> !line.isEmpty() }.size
        counter += linesCount

        showMessage("Syncing $counter/$count")
    }

    private fun showFinishMessage(message: String) {
        EventBus.getDefault().post(SyncFinishedEvent(message))
    }

    private fun showMessage(message: String) {
        EventBus.getDefault().post(SyncEvent(message))
    }

    // TODO: this is temporary thing - remove this after implementing real sync
    private fun openSyncFile() {
        // TODO: enhance this naming
        val dir = mContext.getExternalFilesDir("sync")

        val file = File(dir, "sync.txt")
        fileWriter = FileWriter(file)
    }

    // TODO: this is temporary thing - remove this after implementing real sync
    private fun writeToSyncFile(lines: String) {
        fileWriter?.write(lines)
    }

    // TODO: this is temporary thing - remove this after implementing real sync
    private fun closeSyncFile() {
        fileWriter?.flush()
        fileWriter?.close()
    }

    // TODO: this is temporary thing - remove this after implementing real sync
    private fun checkOutputFileAndShowFinishMessage() {
        val endedAt = System.currentTimeMillis()
        showMessage("Checking downloaded file...")

        val downloadedMessage = "Downloaded $counter/$count."
        var timeMessage = ""
        downloadStartedAt?.let { startedAt ->
            val interval = endedAt - startedAt
            timeMessage = "In ${formatDownloadTimeDuration(interval)}."
        }

        val downloadedFileMessage = if (SyncFileChecker(mContext).run(bleCount, wifiCount, cellularCount)) {
            "Sync file is correct!"
        } else {
            "Something is wrong with downloaded file :/"
        }

        showFinishMessage(
            "Downloading from SD card finished.\n" +
                    "$downloadedMessage\n" +
                    "$timeMessage\n" +
                    downloadedFileMessage
        )
    }

    private fun formatDownloadTimeDuration(duration: Long): String {
        return String.format("%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(duration),
            TimeUnit.MILLISECONDS.toMinutes(duration) -
                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
            TimeUnit.MILLISECONDS.toSeconds(duration) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }
}
