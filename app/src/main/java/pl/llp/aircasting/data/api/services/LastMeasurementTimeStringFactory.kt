package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.util.DateConverter
import java.util.Date

object LastMeasurementTimeStringFactory {
    // BE compares the since cursor against end_time_local (wall-clock numerals
    // persisted via skip_time_zone_conversion_for_attributes). Format in phone-local
    // TZ so the string carries those same numerals.
    fun get(lastMeasurementSyncTime: Date): String =
        DateConverter.toDateString(lastMeasurementSyncTime)
}