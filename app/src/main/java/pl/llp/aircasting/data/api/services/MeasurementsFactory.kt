package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.data.api.response.MeasurementResponse
import pl.llp.aircasting.data.model.Measurement
import java.util.TimeZone

object MeasurementsFactory {
    // BE serializes measurement.time as wall-clock numerals + "Z" suffix (V3 binary
    // ingester writes Utils.to_local_as_utc(epoch, session.time_zone) into the column,
    // then iso8601(3) tags it with Z). The wall clock is the session's TZ on BE: for
    // mapped (outdoor) sessions BE looks up TZ from lat/lng (≈ phone-default in normal
    // use); for indoor / locationless sessions BE defaults session.time_zone to UTC,
    // so the numerals are UTC wall clock and must be parsed as UTC.
    fun get(
        measurementsFromResponse: List<MeasurementResponse>,
        averagingFrequency: Int = 1,
        timeZone: TimeZone = TimeZone.getDefault(),
    ): List<Measurement> = measurementsFromResponse.map {
        Measurement(it, averagingFrequency, timeZone)
    }
}