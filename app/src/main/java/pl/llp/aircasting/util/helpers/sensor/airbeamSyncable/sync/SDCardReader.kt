package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync

import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.util.events.sdcard.SDCardClearFinished
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileService.SDCardFileService

open class SDCardReader(private val sdCardFileService: SDCardFileService) {
    companion object {
        private const val DOWNLOAD_FINISHED = "SD_SYNC_FINISH"
        private const val CLEAR_FINISHED = "SD_DELETE_FINISH"
        private const val SYNC_STEP_PATTERN = "Count"
    }

    private var stepType: StepType? = StepType.MOBILE

    enum class StepType(val value: Int) {
        MOBILE(0),
        FIXED_WIFI(1),
        FIXED_CELLULAR(2);

        fun next(): StepType? {
            return fromInt(value + 1)
        }

        fun fromInt(value: Int) = values().firstOrNull { it.value == value }
    }

    data class Step(val type: StepType, val measurementsCount: Int)

    fun onMetaDataDownloaded(data: ByteArray?) {
        data ?: return
        val valueString = String(data)

        val measurementsInStepCountString = valueString.split(":").lastOrNull()?.trim()

        if (isSyncStep(valueString)) {
            val measurementsInStepCount = measurementsInStepCountString?.toIntOrNull() ?: 0

            stepType?.let {
                val newStep = Step(it, measurementsInStepCount)
                sdCardFileService.transitionTo(newStep)
                stepType = it.next()
            }
        }

        if (valueString == DOWNLOAD_FINISHED) {
            sdCardFileService.finish()
        } else if (valueString == CLEAR_FINISHED) {
            EventBus.getDefault().post(SDCardClearFinished())
        }
    }

    private fun isSyncStep(metaDataString: String): Boolean {
        val metaDataFields = metaDataString.split(":")
        val description = metaDataFields.firstOrNull()?.trim()

        return description?.contains(SYNC_STEP_PATTERN) == true
    }

    fun onMeasurementsDownloaded(dataDownloaded: ByteArray?) {
        dataDownloaded ?: return

        val lines = String(dataDownloaded).lines().filter { it.isNotBlank() }

        sdCardFileService.process(lines)
    }
}
