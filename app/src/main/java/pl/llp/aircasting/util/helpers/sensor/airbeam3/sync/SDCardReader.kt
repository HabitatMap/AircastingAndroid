package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.util.events.sdcard.SDCardClearFinished
import pl.llp.aircasting.util.events.sdcard.SDCardReadEvent
import pl.llp.aircasting.util.events.sdcard.SDCardReadFinished
import pl.llp.aircasting.util.events.sdcard.SDCardReadStepStartedEvent

class SDCardReader() {
    private val DOWNLOAD_FINISHED = "SD_SYNC_FINISH"
    private val CLEAR_FINISHED = "SD_DELETE_FINISH"
    private val COUNTER_STEP_PATTERN = "Count"

    private var stepType: StepType? =
        StepType.MOBILE

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

        if (isCounterStep(valueString)) {
            val measurementsInStepCount = measurementsInStepCountString?.toIntOrNull() ?: 0

            if (stepType != null && measurementsInStepCount != null) {
                EventBus.getDefault().post(
                    SDCardReadStepStartedEvent(Step(stepType!!, measurementsInStepCount))
                )
                stepType = stepType?.next()
            }
        }

        if (valueString == DOWNLOAD_FINISHED) {
            EventBus.getDefault().post(SDCardReadFinished())
        } else if (valueString == CLEAR_FINISHED) {
            EventBus.getDefault().post(SDCardClearFinished())
        }
    }

    private fun isCounterStep(metaDataString: String): Boolean {
        val metaDataFields = metaDataString.split(":")
        val description = metaDataFields.firstOrNull()?.trim()

        return description?.contains(COUNTER_STEP_PATTERN) == true
    }

    fun onMeasurementsDownloaded(data: ByteArray?) {
        data ?: return

        val lines = String(data).lines().filter { line -> !line.isBlank() }

        EventBus.getDefault().post(
            SDCardReadEvent(
                lines
            )
        )
    }
}
