package io.lunarlogic.aircasting.sensor.airbeam3

import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.sensor.ResponseParser
import no.nordicsemi.android.ble.data.Data
import org.greenrobot.eventbus.EventBus

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

        if (!dataString.isEmpty()) {
            val newMeasurementEvent = responseParser.parse(dataString)
            newMeasurementEvent?.let { EventBus.getDefault().post(newMeasurementEvent) }
        }
    }
}
