package pl.llp.aircasting.util

import pl.llp.aircasting.util.extensions.calendar
import java.util.*

class CalendarUtils {
    companion object {
        private val mCalendar = calendar()

        fun dayOfMonth(date: Date): Int {
            mCalendar.time = date
            return mCalendar[Calendar.DAY_OF_MONTH]
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
        fun getStartOfTodaySecondsFromYearAgo(): Long {
            val today = calendar()
            today.apply {
                timeZone = TimeZone.getTimeZone("UTC")
                add(Calendar.YEAR, -1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return today.timeInMillis / 1000
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
        fun getEndOfTodaySeconds(): Long {
            val today = calendar()
            today.apply {
                timeZone = TimeZone.getTimeZone("UTC")
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 0)
            }
            return today.timeInMillis / 1000
        }
    }
}
