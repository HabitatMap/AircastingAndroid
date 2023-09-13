package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileService

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
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardReader
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.SDCardCSVFileFactory
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class SDCardFileServiceFactory @Inject constructor(
    private val aBMiniSDCardFileService: ABMiniSDCardFileService,
    private val aB3SDCardFileService: AB3SDCardFileService
) {
    fun create(deviceType: DeviceItem.Type) = when(deviceType) {
        DeviceItem.Type.AIRBEAMMINI -> aBMiniSDCardFileService
        else -> aB3SDCardFileService
    }
}
abstract class SDCardFileService(
    private val scope: CoroutineScope,
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

    private val newLinesFlow = MutableSharedFlow<List<String>>()

    init {
        EventBus.getDefault().safeRegister(this)
    }

    fun start(
        onDownloadFinished: (stepByFilePaths: Map<SDCardReader.Step?, List<String>>) -> Unit
    ) {
        mOnDownloadFinished = onDownloadFinished

        steps = ArrayList()
        stepByFilePaths = mutableMapOf()
        currentSessionUUID = null

        newLinesFlow.onEach { lines ->
            process(lines)
        }.launchIn(scope)
    }

    protected abstract fun process(lines: List<String>)

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
        counter.set(0)
        steps.add(event.step)
        stepByFilePaths[currentStep] = mutableListOf()
    }

    @Subscribe
    fun onEvent(event: SDCardReadEvent) {
        val fourLinesOfMeasurements = event.lines

        scope.launch {
            newLinesFlow.emit(fourLinesOfMeasurements)
        }
    }

    @Subscribe
    fun onEvent(event: SDCardReadFinished) {
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
