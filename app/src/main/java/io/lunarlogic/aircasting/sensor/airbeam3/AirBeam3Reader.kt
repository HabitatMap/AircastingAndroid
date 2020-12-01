package io.lunarlogic.aircasting.sensor.airbeam3

import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.sensor.ResponseParser
import no.nordicsemi.android.ble.data.Data
import org.greenrobot.eventbus.EventBus

class AirBeam3Reader(
    errorHandler: ErrorHandler
) {
    val responseParser = ResponseParser(errorHandler)

    fun run(data: Data) {
        val value = data.value ?: return

        val dataString = String(value)

        if (!dataString.isEmpty()) {
            val newMeasurementEvent = responseParser.parse(dataString)
            newMeasurementEvent?.let {
                EventBus.getDefault().post(newMeasurementEvent)
            }
        }
    }
}
