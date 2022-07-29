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
         * This method returns the Start of the current day - year ago.
         * timeZone is UTC.
         * example: if the current day is Aug 1, the method returns:
         * GMT: Sunday, August 1, 2021 12:00:00 AM
         * Our time zone: Sunday, August 1, 2021 2:00:00 AM GMT+02:00 DST
         **/
        fun getStartOfTodayMillisFromYearAgo(): Long {
            val today = calendar()
            today.apply {
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
         * example: if the current day is Aug 1, the method returns:
         * GMT: Monday, August 1, 2022 11:59:59 PM
         * Our time zone: Tuesday, August 2, 2022 1:59:59 AM GMT+02:00 DST
         **/
        fun getEndOfTodayMillis(): Long {
            val today = calendar()
            today.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 0)
            }
            return today.timeInMillis / 1000
        }
    }
}
