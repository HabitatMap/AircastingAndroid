package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.data.Data
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.di.modules.MainScope
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.sensor.ResponseParser
import javax.inject.Inject

/**
 * This reader will be effectively use only in MOBILE sessions
 * Only when AirBeam is in the MOBILE mode the data is sent to the Android device
 * No NewMeasurementEvent will be posted for FIXED sessions
 */

class AirBeam3Reader @Inject constructor(
    context: Context,
    errorHandler: ErrorHandler
) {
    @Inject
    @MainScope
    lateinit var coroutineScope: CoroutineScope

    @Inject
    lateinit var batteryLevelFlow: MutableSharedFlow<Int>

    private val responseParser = ResponseParser(errorHandler)

    init {
        (context as AircastingApplication).userDependentComponent?.inject(this) //TODO: ?
    }

    fun run(data: Data) {
        val value = data.value ?: return

        val dataString = String(value)

        if (dataString.isNotEmpty()) {
            val batteryPercentage = getBatteryLevelOrNull(dataString)

            if (batteryPercentage != null) {
                updateBatteryLevel(batteryPercentage)
            } else {
                postNewMeasurementEvent(dataString)
            }
        }
    }

    private fun getBatteryLevelOrNull(dataString: String): Int? {
        val regex = """(\d+)%""".toRegex()
        val matchResult = regex.find(dataString)
        return if (matchResult != null) {
            val (percentage) = matchResult.destructured
            percentage.toIntOrNull()
        } else null
    }

    private fun updateBatteryLevel(percentage: Int) {
        coroutineScope.launch {
            batteryLevelFlow.emit(percentage)
        }
    }

    private fun postNewMeasurementEvent(measurement: String) {
        responseParser.parse(measurement)?.let { EventBus.getDefault().post(it) }
    }
}
