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

fun Calendar.getStartOfTodaySecondsFromYearAgo(): Long {
    // UTC timezone here is ESSENTIAL, as backend assumes receiving timestamp in UTC
    timeZone = TimeZone.getTimeZone("UTC")
    add(YEAR, -1)
    set(HOUR_OF_DAY, 0)
    set(MINUTE, 0)
    set(SECOND, 0)
    set(MILLISECOND, 0)

    return timeInMillis / 1000
}

fun Calendar.getEndOfTodaySeconds(): Long {
    // UTC timezone here is ESSENTIAL, as backend assumes receiving timestamp in UTC
    timeZone = TimeZone.getTimeZone("UTC")
    set(HOUR_OF_DAY, 23)
    set(MINUTE, 59)
    set(SECOND, 59)
    set(MILLISECOND, 0)

    return timeInMillis / 1000
}