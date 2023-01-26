package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import android.content.Context
import android.util.Log
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.util.events.sdcard.SDCardReadEvent
import pl.llp.aircasting.util.events.sdcard.SDCardReadFinished
import pl.llp.aircasting.util.events.sdcard.SDCardReadStepStartedEvent
import pl.llp.aircasting.util.extensions.safeRegister
import java.io.FileWriter

class SDCardDownloadService(mContext: Context) {
    private val DOWNLOAD_TAG = "SYNC"
    private val mCSVFileFactory = SDCardCSVFileFactory(mContext)

    private var fileWriter: FileWriter? = null
    private var counter = 0
    private var steps: ArrayList<SDCardReader.Step> = ArrayList()

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
        val files =
            listOf(mCSVFileFactory.getMobileDirectory(), mCSVFileFactory.getFixedDirectory())
        files.forEach { file ->
            if (file?.exists() == true) file.delete()
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
        val lines = event.lines
        val linesString = lines.map { "$it\n" }.joinToString("")
        writeToSyncFile(linesString)

        val linesCount = lines.size
        counter += linesCount

        val step = steps.lastOrNull()
        step?.let { mOnLinesDownloaded?.invoke(it, counter) }
    }

    @Subscribe
    fun onEvent(event: SDCardReadFinished) {
        Log.d(DOWNLOAD_TAG, "Sync finished")
        closeSyncFile()

        mOnDownloadFinished?.invoke(steps)
    }

    private fun openSyncFileDirectory(step: SDCardReader.Step) {
        val stepType = step.type
        when (stepType) {
            SDCardReader.StepType.MOBILE -> {
                openSyncFileDirectory(stepType)
            }
            SDCardReader.StepType.FIXED_WIFI -> {
                closeSyncFile()
                openSyncFileDirectory(stepType)
            }
            SDCardReader.StepType.FIXED_CELLULAR -> {
                // do nothing
                // should just append to the previous file
            }
        }
    }

    private fun openSyncFileDirectory(stepType: SDCardReader.StepType) {
        val file = mCSVFileFactory.getDirectory(stepType)
        fileWriter = try {
            FileWriter(file)
        } catch (e: Exception) {
            null
        }
    }

    private fun writeToSyncFile(lines: String) {
        fileWriter?.write(lines)
    }

    private fun closeSyncFile() {
        fileWriter?.flush()
        fileWriter?.close()
    }
}
