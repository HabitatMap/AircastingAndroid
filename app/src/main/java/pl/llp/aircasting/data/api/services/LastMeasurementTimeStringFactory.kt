package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.util.DateConverter
import java.util.Date
import java.util.TimeZone

object LastMeasurementTimeStringFactory {
    // BE compares the since cursor against the wall-clock numerals it stored for the
    // session's measurements. For mapped (outdoor) sessions that wall clock is the
    // session's geo TZ (≈ phone-default in normal use); for indoor / locationless
    // sessions BE defaults session.time_zone to UTC, so the cursor must be formatted
    // as UTC to compare correctly.
    fun get(
        lastMeasurementSyncTime: Date,
        timeZone: TimeZone = TimeZone.getDefault(),
    ): String = DateConverter.toDateString(lastMeasurementSyncTime, timeZone)
}