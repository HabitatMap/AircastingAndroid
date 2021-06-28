package pl.llp.aircasting.sensor.airbeam3.sync

import pl.llp.aircasting.events.sdcard.SDCardClearFinished
import pl.llp.aircasting.events.sdcard.SDCardReadEvent
import pl.llp.aircasting.events.sdcard.SDCardReadFinished
import pl.llp.aircasting.events.sdcard.SDCardReadStepStartedEvent
import org.greenrobot.eventbus.EventBus

class SDCardReader {
    private val DOWNLOAD_FINISHED = "SD_SYNC_FINISH"
    private val CLEAR_FINISHED = "SD_DELETE_FINISH"

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

    class Step(val type: StepType, val measurementsCount: Int)

    fun onMetaDataDownloaded(data: ByteArray?) {
        data ?: return
        val valueString = String(data)

        try {
            val measurementsInStepCountString = valueString.split(":").lastOrNull()?.trim()
            val measurementsInStepCount = if (measurementsInStepCountString?.contains("⸮") == true) {
                0
            } else {
                measurementsInStepCountString?.toInt()
            }

            if (stepType != null && measurementsInStepCount != null) {
                EventBus.getDefault().post(
                    SDCardReadStepStartedEvent(Step(stepType!!, measurementsInStepCount))
                )
                stepType = stepType?.next()
            }
        } catch (e: NumberFormatException) {
            // ignore - this is e.g. SD_SYNC_FINISH
        }

        if (valueString == DOWNLOAD_FINISHED) {
            EventBus.getDefault().post(SDCardReadFinished())
        } else if (valueString == CLEAR_FINISHED) {
            EventBus.getDefault().post(SDCardClearFinished())
        }
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
