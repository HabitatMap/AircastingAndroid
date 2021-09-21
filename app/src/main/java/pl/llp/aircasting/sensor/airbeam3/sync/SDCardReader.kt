package pl.llp.aircasting.sensor.airbeam3.sync

import pl.llp.aircasting.events.sdcard.SDCardClearFinished
import pl.llp.aircasting.events.sdcard.SDCardReadEvent
import pl.llp.aircasting.events.sdcard.SDCardReadFinished
import pl.llp.aircasting.events.sdcard.SDCardReadStepStartedEvent
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.exceptions.SDCardSyncError

class SDCardReader(errorHandler: ErrorHandler) {
    private val DOWNLOAD_FINISHED = "SD_SYNC_FINISH"
    private val CLEAR_FINISHED = "SD_DELETE_FINISH"
    private val COUNTER_STEP_PATTERN = "Count"
    private val mErrorHandler = errorHandler

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

        mErrorHandler.handle(SDCardSyncError("SDCardReader,onMetaDataDownloaded, valueString: ${valueString} "))
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
            mErrorHandler.handle(SDCardSyncError("SDCardReader,onMetaDataDownloaded,DOWNLOAD_FINISHED! "))
            EventBus.getDefault().post(SDCardReadFinished())
        } else if (valueString == CLEAR_FINISHED) {
            mErrorHandler.handle(SDCardSyncError("SDCardReader,onMetaDataDownloaded,CLEAR_FINISHED! "))
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
