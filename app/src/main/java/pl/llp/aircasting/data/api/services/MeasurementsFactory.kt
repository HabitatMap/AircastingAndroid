package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.data.api.response.MeasurementResponse
import pl.llp.aircasting.data.model.Measurement
import java.util.TimeZone

object MeasurementsFactory {
    // BE serializes measurement.time as wall-clock numerals + "Z" suffix (V3 binary
    // ingester writes Utils.to_local_as_utc(epoch, session.time_zone) into the column,
    // then iso8601(3) tags it with Z). Parse in phone-local TZ so Date.time is the
    // real instant for that wall clock.
    fun get(
        measurementsFromResponse: List<MeasurementResponse>,
        averagingFrequency: Int = 1,
    ): List<Measurement> = measurementsFromResponse.map {
        Measurement(it, averagingFrequency, TimeZone.getDefault())
    }
}