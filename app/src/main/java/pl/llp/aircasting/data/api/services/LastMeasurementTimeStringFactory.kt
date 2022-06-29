package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.util.DateConverter
import java.util.*

object LastMeasurementTimeStringFactory {
    fun get(
        lastMeasurementSyncTime: Date,
        isExternal: Boolean
    ): String {
        return if (!isExternal) DateConverter.toDateString(lastMeasurementSyncTime)
        else DateConverter.toDateString(lastMeasurementSyncTime, TimeZone.getTimeZone("UTC"))
    }
}