package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable

import no.nordicsemi.android.ble.data.Data
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.sensor.ResponseParser

/**
 * This reader will be effectively use only in MOBILE sessions
 * Only when AirBeam is in the MOBILE mode the data is sent to the Android device
 * No NewMeasurementEvent will be posted for FIXED sessions
 */

class AirBeam3Reader(
    errorHandler: ErrorHandler
) {
    val responseParser = ResponseParser(errorHandler)

    fun run(data: Data) {
        val value = data.value ?: return

        val dataString = String(value)

        if (dataString.isNotEmpty()) {
            val newMeasurementEvent = responseParser.parse(dataString)
            newMeasurementEvent?.let { EventBus.getDefault().post(newMeasurementEvent) }
        }
    }
}
