package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileService

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.di.modules.IoCoroutineScope
import pl.llp.aircasting.util.events.sdcard.SDCardLinesReadEvent
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.SDCardCSVFileFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.lineParameter.CSVLineParameterHandler
import javax.inject.Inject

class AB3SDCardFileService @Inject constructor(
    @IoCoroutineScope private val scope: CoroutineScope,
    mCSVFileFactory: SDCardCSVFileFactory,
) : SDCardFileService(scope, mCSVFileFactory) {
    override fun process(lines: List<String>) {
        incrementCounter(lines)

        lines.forEach { line ->
            val lineParams = CSVLineParameterHandler.lineParameters(line)
            if (sessionHasChanged(lineParams)) {
                flashLinesInBufferAndCloseCurrentFile()
                currentSessionUUID = lineParams[1]
                createAndOpenNewFile()
            }
            try {
                fileWriter?.write("$line\n")
            } catch (e: Exception) {
                Log.e(TAG, e.stackTraceToString())
            }
        }
    }

    private fun incrementCounter(lines: List<String>) {
        val linesCount = lines.size
        counter.addAndGet(linesCount)
        Log.d("SDSync Counter", "Step: ${currentStep?.type.toString()}\nValue: ${counter.get()} / ${currentStep?.measurementsCount}")

        currentStep?.let {
            val event = SDCardLinesReadEvent(it, counter.get())
            EventBus.getDefault().post(event)
        }
    }

    private fun sessionHasChanged(lineParams: List<String>) = lineParams[1] != currentSessionUUID
}