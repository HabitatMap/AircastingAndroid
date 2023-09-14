package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileService

import android.util.Log
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardReader
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.SDCardCSVFileFactory
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@UserSessionScope
class SDCardFileServiceProvider @Inject constructor(
    private val aBMiniSDCardFileService: ABMiniSDCardFileService,
    private val aB3SDCardFileService: AB3SDCardFileService
) {
    fun get(deviceType: DeviceItem.Type) = when(deviceType) {
        DeviceItem.Type.AIRBEAMMINI -> aBMiniSDCardFileService
        else -> aB3SDCardFileService
    }
}
abstract class SDCardFileService(
    private val mCSVFileFactory: SDCardCSVFileFactory,
) {

    protected var fileWriter: FileWriter? = null
    protected var counter = AtomicInteger()
    private var steps: ArrayList<SDCardReader.Step> = ArrayList()
    protected val currentStep get() = steps.lastOrNull()

    private var stepByFilePaths = mutableMapOf<SDCardReader.Step?, MutableList<String>>()

    protected var currentSessionUUID: String? = null
    private val currentFilePath get() = "${mCSVFileFactory.getDirectory(currentStep?.type)}/$currentSessionUUID.csv"

    private var mOnDownloadFinished: ((measurementsPerSession: Map<SDCardReader.Step?, List<String>>) -> Unit)? =
        null

    abstract fun process(lines: List<String>)

    fun setup(
        onDownloadFinished: (stepByFilePaths: Map<SDCardReader.Step?, List<String>>) -> Unit
    ) {
        mOnDownloadFinished = onDownloadFinished

        steps = ArrayList()
        stepByFilePaths = mutableMapOf()
        currentSessionUUID = null
    }


    fun deleteAllSyncFiles() {
        val dirs = listOf(
            mCSVFileFactory.getMobileDirectory(),
            mCSVFileFactory.getFixedDirectory()
        )
        dirs.forEach { dir ->
            if (dir?.exists() == true) {
                val result = dir.deleteRecursively()
                Log.v(TAG, "${dir.name.split("/").last()} files were deleted: $result")
            }
        }
    }

    fun transitionTo(step: SDCardReader.Step) {
        counter.set(0)
        steps.add(step)
        stepByFilePaths[currentStep] = mutableListOf()
    }

    fun finish() {
        Log.d(TAG, "Sync finished")

        flashLinesInBufferAndCloseCurrentFile()

        mOnDownloadFinished?.invoke(stepByFilePaths)
    }

    protected fun flashLinesInBufferAndCloseCurrentFile() {
        try {
            fileWriter?.flush()
            fileWriter?.close()
        } catch (e: IOException) {
            Log.e(TAG, e.stackTraceToString())
        }
    }

    protected fun createAndOpenNewFile() {
        try {
            val file = File(currentFilePath)
            Log.v(TAG, "Creating file: $file")
            fileWriter = FileWriter(file)

            stepByFilePaths[currentStep]?.add(currentFilePath)
        } catch (e: Exception) {
            Log.e(TAG, e.stackTraceToString())
        }
    }
}
