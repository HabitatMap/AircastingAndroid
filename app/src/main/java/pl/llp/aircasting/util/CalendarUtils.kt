package pl.llp.aircasting.util

import java.util.*

class CalendarUtils {
    companion object {
        private val mCalendar = Calendar.getInstance()

        fun dayOfMonth(date: Date): Int {
            mCalendar.time = date
            return mCalendar[Calendar.DAY_OF_MONTH]
        }
    }
}
