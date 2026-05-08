package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.data.api.response.MeasurementResponse
import pl.llp.aircasting.data.model.Measurement
import java.util.TimeZone

object MeasurementsFactory {
    private val UTC = TimeZone.getTimeZone("UTC")

    fun get(
        measurementsFromResponse: List<MeasurementResponse>,
        averagingFrequency: Int = 1,
    ): List<Measurement> = measurementsFromResponse.map {
        Measurement(it, averagingFrequency, UTC)
    }
}