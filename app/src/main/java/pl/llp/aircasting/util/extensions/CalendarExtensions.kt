package pl.llp.aircasting.util.extensions

import java.util.*
import java.util.Calendar.*

fun calendar(): Calendar = getInstance()

fun Calendar.addHours(date: Date, hours: Int): Date {
    time = date
    add(HOUR_OF_DAY, hours)
    return time
}

fun Calendar.dayOfMonth(date: Date, isExternalSession: Boolean): Int {
    time = date
    if (isExternalSession)
        timeZone = TimeZone.getTimeZone("UTC")
    return this[DAY_OF_MONTH]
}
/**
 * This extension function is used to set left time boundary for searching of sessions on Google Map in S&F feature.
 * Setting UTC time zone is essential, as all time communicated to backend is assumed to be in UTC time zone
 * Example:
 *
 * 08/09/2022 10:30:00 GMT+2 - Current local timestamp
 *
 * 08/09/2021 00:00:00 GMT+0 - Result - start of current day a year ago in UTC
 * @return 1631059200
 */
fun Calendar.getStartOfTodayEpochFromYearAgo(): Long {
    timeZone = TimeZone.getTimeZone("UTC")
    add(YEAR, -1)
    set(HOUR_OF_DAY, 0)
    set(MINUTE, 0)
    set(SECOND, 0)
    set(MILLISECOND, 0)

    return timeInMillis / 1000
}
/**
 * This extension function is used to set right time boundary for searching of sessions on Google Map in S&F feature.
 * Setting UTC time zone is essential, as all time communicated to backend is assumed to be in UTC time zone
 * Example:
 *
 * 08/09/2022 10:30:00 GMT+2 - Current local timestamp
 *
 * 08/09/2022 23:59:59 GMT+0 - Result - End of current day a year ago in UTC
 * @return 1631059200
 */
fun Calendar.getEndOfTodayEpoch(): Long {
    timeZone = TimeZone.getTimeZone("UTC")
    set(HOUR_OF_DAY, 23)
    set(MINUTE, 59)
    set(SECOND, 59)
    set(MILLISECOND, 0)

    return timeInMillis / 1000
}