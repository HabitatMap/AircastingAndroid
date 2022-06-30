package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.data.api.response.MeasurementResponse
import pl.llp.aircasting.data.model.Measurement
import java.util.*

object MeasurementsFactory {
    fun get(
        measurementsFromResponse: List<MeasurementResponse>,
        averagingFrequency: Int = 1,
        isExternal: Boolean = false
    ): List<Measurement> {
        return if (!isExternal) measurementsFromResponse.map {
            Measurement(it, averagingFrequency)
        } else
            measurementsFromResponse.map {
                Measurement(it, averagingFrequency, TimeZone.getTimeZone("UTC"))
            }
    }
}