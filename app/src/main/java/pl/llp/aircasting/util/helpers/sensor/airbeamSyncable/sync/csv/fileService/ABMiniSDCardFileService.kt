package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileService

import android.util.Log
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.util.events.sdcard.SDCardLinesReadEvent
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.SDCardCSVFileFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.lineParameter.CSVLineParameterHandler
import javax.inject.Inject

@UserSessionScope
class ABMiniSDCardFileService @Inject constructor(
    mCSVFileFactory: SDCardCSVFileFactory,
) : SDCardFileService(mCSVFileFactory) {
    override fun process(lines: List<String>) {
        lines.forEach { line ->
            val lineParams = CSVLineParameterHandler.lineParameters(line)
            if (sessionHasChanged(lineParams)) {
                flashLinesInBufferAndCloseCurrentFile()
                currentSessionUUID = lineParams[0]
                createAndOpenNewFile()
                return@forEach
            }
            try {
                incrementCounter()
                fileWriter?.write("$currentSessionUUID,$line\n")
            } catch (e: Exception) {
                Log.e(TAG, e.stackTraceToString())
            }
        }
    }

    private fun incrementCounter() {
        counter.incrementAndGet()
        Log.d("SDSync Counter", "Step: ${currentStep?.type.toString()}\nValue: ${counter.get()} / ${currentStep?.measurementsCount}")

        currentStep?.let {
            val event = SDCardLinesReadEvent(it, counter.get())
            EventBus.getDefault().post(event)
        }
    }

    private fun sessionHasChanged(lineParams: List<String>) = lineParams.size == 1
}