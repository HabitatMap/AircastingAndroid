package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import android.content.Context
import android.util.Log
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.util.events.sdcard.SDCardReadEvent
import pl.llp.aircasting.util.events.sdcard.SDCardReadFinished
import pl.llp.aircasting.util.events.sdcard.SDCardReadStepStartedEvent
import pl.llp.aircasting.util.extensions.safeRegister
import java.io.File
import java.io.FileWriter

class SDCardDownloadService(mContext: Context) {
    companion object {
        private const val DOWNLOAD_TAG = "SYNC"
        private const val AB_DELIMITER = ","
    }

    private val mCSVFileFactory = SDCardCSVFileFactory(mContext)

    private var fileWriter: FileWriter? = null
    private var counter = 0
    private var steps: ArrayList<SDCardReader.Step> = ArrayList()
    private val currentStep get() = steps.lastOrNull()

    private var currentSessionUUID: String? = null

    private var mOnDownloadFinished: ((steps: List<SDCardReader.Step>) -> Unit)? = null
    private var mOnLinesDownloaded: ((step: SDCardReader.Step, linesCount: Int) -> Unit)? = null

    init {
        EventBus.getDefault().safeRegister(this)
    }

    fun run(
        onLinesDownloaded: (step: SDCardReader.Step, linesCount: Int) -> Unit,
        onDownloadFinished: (steps: List<SDCardReader.Step>) -> Unit
    ) {
        mOnLinesDownloaded = onLinesDownloaded
        mOnDownloadFinished = onDownloadFinished

        steps = ArrayList()
    }

    fun deleteFiles() {
        val dirs = listOf(
            mCSVFileFactory.getMobileDirectory(),
            mCSVFileFactory.getFixedDirectory()
        )
        dirs.forEach { dir ->
            if (dir?.exists() == true) {
                val result = dir.deleteRecursively()
                Log.v(TAG, "${dir.name} was deleted: $result")
            }
        }
    }

    @Subscribe
    fun onEvent(event: SDCardReadStepStartedEvent) {
        counter = 0
        val step = event.step
        steps.add(step)

        openSyncFile(step)
    }

    @Subscribe
    fun onEvent(event: SDCardReadEvent) {
        val fourLinesOfMeasurements = event.lines

        writeToCorrespondingFile(fourLinesOfMeasurements)

        val linesCount = fourLinesOfMeasurements.size
        counter += linesCount

        currentStep?.let { mOnLinesDownloaded?.invoke(it, counter) }
    }

    @Subscribe
    fun onEvent(event: SDCardReadFinished) {
        Log.d(DOWNLOAD_TAG, "Sync finished")

        flashLinesInBufferAndCloseCurrentFile()

        mOnDownloadFinished?.invoke(steps)
    }

    private fun writeToCorrespondingFile(lines: List<String>) = lines.forEach { line ->
        val lineParams = line.split(AB_DELIMITER)
        val uuid = lineParams[1]

        if (uuid != currentSessionUUID) {
            currentSessionUUID = uuid
            flashLinesInBufferAndCloseCurrentFile()
            createAndOpenNewFile(uuid)
        }
        fileWriter?.write("$line\n")
    }

    private fun flashLinesInBufferAndCloseCurrentFile() {
        fileWriter?.flush()
        fileWriter?.close()
    }

    private fun createAndOpenNewFile(sessionUUID: String) {
        val file = File(mCSVFileFactory.getDirectory(currentStep?.type), sessionUUID)
        fileWriter = FileWriter(file)
    }
}
