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
 * This method returns the Start of the current day - a year ago in milliseconds.
 * timeZone is UTC.
 * EXAMPLE: if the current day is Aug 2:
 * @return "1627862400"
 * Assuming that this timestamp is in seconds:
 * GMT: Monday, August 2, 2021 12:00:00 AM
 * Your time zone: Monday, August 2, 2021 2:00:00 AM GMT+02:00 DST
 * Relative: A year ago
 **/
fun Calendar.getStartOfTodaySecondsFromYearAgo(): Long {
    timeZone = TimeZone.getTimeZone("UTC")
    add(YEAR, -1)
    set(HOUR_OF_DAY, 0)
    set(MINUTE, 0)
    set(SECOND, 0)
    set(MILLISECOND, 0)

    return timeInMillis / 1000
}
/**
 * This method returns the end of the current day in milliseconds.
 * timeZone is UTC.
 * EXAMPLE: if the current day is Aug 2, the method returns:
 * @return "1659484799"
 * Assuming that this timestamp is in seconds:
 * GMT: Tuesday, August 2, 2022 11:59:59 PM
 * Your time zone: Wednesday, August 3, 2022 1:59:59 AM GMT+02:00 DST
 * Relative: In 13 hours
 **/
fun Calendar.getEndOfTodaySeconds(): Long {
    timeZone = TimeZone.getTimeZone("UTC")
    set(HOUR_OF_DAY, 23)
    set(MINUTE, 59)
    set(SECOND, 59)
    set(MILLISECOND, 0)

    return timeInMillis / 1000
}