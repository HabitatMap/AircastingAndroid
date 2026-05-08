package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.util.DateConverter
import java.util.Date
import java.util.TimeZone

object LastMeasurementTimeStringFactory {
    private val UTC = TimeZone.getTimeZone("UTC")

    fun get(lastMeasurementSyncTime: Date): String =
        DateConverter.toDateString(lastMeasurementSyncTime, UTC)
}