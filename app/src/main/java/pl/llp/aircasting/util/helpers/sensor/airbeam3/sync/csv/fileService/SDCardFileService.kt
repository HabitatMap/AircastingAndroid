package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv.fileService

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.events.sdcard.SDCardReadEvent
import pl.llp.aircasting.util.events.sdcard.SDCardReadFinished
import pl.llp.aircasting.util.events.sdcard.SDCardReadStepStartedEvent
import pl.llp.aircasting.util.extensions.safeRegister
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.SDCardReader
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv.SDCardCSVFileFactory
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv.lineParameter.CSVLineParameterHandler
import java.io.File
import java.io.FileWriter
import java.io.IOException

abstract class SDCardFileService(
    private val scope: CoroutineScope,
    private val mCSVFileFactory: SDCardCSVFileFactory,
) {

    private var fileWriter: FileWriter? = null
    private var counter = 0
    private var steps: ArrayList<SDCardReader.Step> = ArrayList()
    private val currentStep get() = steps.lastOrNull()

    private var stepByFilePaths = mutableMapOf<SDCardReader.Step?, MutableList<String>>()

    protected var currentSessionUUID: String? = null
    private val currentFilePath get() = "${mCSVFileFactory.getDirectory(currentStep?.type)}/$currentSessionUUID.csv"

    private var mOnDownloadFinished: ((measurementsPerSession: Map<SDCardReader.Step?, List<String>>) -> Unit)? =
        null
    private var mOnLinesDownloaded: ((step: SDCardReader.Step, linesCount: Int) -> Unit)? = null

    private val newLinesFlow = MutableSharedFlow<List<String>>()

    init {
        EventBus.getDefault().safeRegister(this)
    }

    fun start(
        deviceItem: DeviceItem,
        onLinesDownloaded: (step: SDCardReader.Step, linesCount: Int) -> Unit,
        onDownloadFinished: (stepByFilePaths: Map<SDCardReader.Step?, List<String>>) -> Unit
    ) {
        mOnLinesDownloaded = onLinesDownloaded
        mOnDownloadFinished = onDownloadFinished

        steps = ArrayList()
        stepByFilePaths = mutableMapOf()
        currentSessionUUID = null

        newLinesFlow.onEach { lines ->
            lines.forEach { line ->
                val lineParams = CSVLineParameterHandler.lineParameters(line)
                if (sessionHasChanged(lineParams, deviceItem)) {
                    flashLinesInBufferAndCloseCurrentFile()
                    currentSessionUUID = getNewSessionUUID(lineParams)
                    createAndOpenNewFile()
                }
                try {
                    fileWriter?.write("${getLine(line)}\n")
                } catch (e: Exception) {
                    Log.e(TAG, e.stackTraceToString())
                }
            }
        }.launchIn(scope)
    }

    protected abstract fun getLine(line: String): String

    protected abstract fun getNewSessionUUID(lineParams: List<String>): String


    protected abstract fun sessionHasChanged(
        lineParams: List<String>,
        deviceItem: DeviceItem
    ): Boolean

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

    @Subscribe
    fun onEvent(event: SDCardReadStepStartedEvent) {
        counter = 0
        steps.add(event.step)
        stepByFilePaths[currentStep] = mutableListOf()
    }

    @Subscribe
    fun onEvent(event: SDCardReadEvent) {
        val fourLinesOfMeasurements = event.lines

        scope.launch {
            newLinesFlow.emit(fourLinesOfMeasurements)
        }

        val linesCount = fourLinesOfMeasurements.size
        counter += linesCount

        currentStep?.let { mOnLinesDownloaded?.invoke(it, counter) }
    }

    @Subscribe
    fun onEvent(event: SDCardReadFinished) {
        Log.d(TAG, "Sync finished")

        flashLinesInBufferAndCloseCurrentFile()

        mOnDownloadFinished?.invoke(stepByFilePaths)
    }

    private fun flashLinesInBufferAndCloseCurrentFile() {
        try {
            fileWriter?.flush()
            fileWriter?.close()
        } catch (e: IOException) {
            Log.e(TAG, e.stackTraceToString())
        }
    }

    private fun createAndOpenNewFile() {
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
