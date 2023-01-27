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

class SDCardFileService(mContext: Context) {
    companion object {
        private const val DOWNLOAD_TAG = "SYNC"
        private const val AB_DELIMITER = ","
    }

    private val mCSVFileFactory = SDCardCSVFileFactory(mContext)

    private var fileWriter: FileWriter? = null
    private var counter = 0
    private var steps: ArrayList<SDCardReader.Step> = ArrayList()
    private val currentStep get() = steps.lastOrNull()

    private val filePathByMeasurementsCount = mutableMapOf<String, Int>()

    private var currentSessionUUID: String? = null
    private val currentFilePath get() = "${mCSVFileFactory.getDirectory(currentStep?.type)}/$currentSessionUUID.csv"

    private var mOnDownloadFinished: ((measurementsPerSession: Map<String, Int>) -> Unit)? = null
    private var mOnLinesDownloaded: ((step: SDCardReader.Step, linesCount: Int) -> Unit)? = null

    init {
        EventBus.getDefault().safeRegister(this)
    }

    fun run(
        onLinesDownloaded: (step: SDCardReader.Step, linesCount: Int) -> Unit,
        onDownloadFinished: (measurementsPerSession: Map<String, Int>) -> Unit
    ) {
        mOnLinesDownloaded = onLinesDownloaded
        mOnDownloadFinished = onDownloadFinished

        steps = ArrayList()
    }

    fun deleteAllSyncFiles() {
        val dirs = listOf(
            mCSVFileFactory.getMobileDirectory(),
            mCSVFileFactory.getFixedDirectory()
        )
        dirs.forEach { dir ->
            if (dir?.exists() == true) {
                val result = dir.deleteRecursively()
                Log.v(TAG, "${dir.name.split("/").last()} was deleted: $result")
            }
        }
    }

    fun delete(file: File) {
        val result = file.delete()
        Log.v(TAG, "${file.name.split("/").last()} was deleted: $result")
    }

    @Subscribe
    fun onEvent(event: SDCardReadStepStartedEvent) {
        counter = 0
        steps.add(event.step)
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
        Log.v(TAG, filePathByMeasurementsCount.toString())

        mOnDownloadFinished?.invoke(filePathByMeasurementsCount)
    }

    private fun writeToCorrespondingFile(lines: List<String>) = lines.forEach { line ->
        Log.v(TAG, "Reading line: $line")

        val lineParams = line.split(AB_DELIMITER)
        val uuid = lineParams[1]

        if (sessionHasChanged(uuid)) {
            flashLinesInBufferAndCloseCurrentFile()
            currentSessionUUID = uuid
            createAndOpenNewFile()
            filePathByMeasurementsCount[currentFilePath] = 1
        } else {
            filePathByMeasurementsCount[currentFilePath]?.plus(1)
        }
        fileWriter?.write("$line\n")
    }

    private fun sessionHasChanged(uuid: String) = uuid != currentSessionUUID

    private fun flashLinesInBufferAndCloseCurrentFile() {
        fileWriter?.flush()
        fileWriter?.close()
    }

    private fun createAndOpenNewFile() {
        val file = File(currentFilePath)
        Log.v(TAG, "Creating file: $file")
        fileWriter = FileWriter(file)
    }
}
