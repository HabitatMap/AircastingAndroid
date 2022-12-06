package pl.llp.aircasting.util

import java.util.*
import java.util.concurrent.TimeUnit

class TimezoneHelper {
    companion object {
        fun getTimezoneOffsetInHours(): Int {
            return TimeUnit.HOURS.convert(getTimezoneOffsetInMillis().toLong(), TimeUnit.MILLISECONDS).toInt()
        }

        fun getTimezoneOffsetInSeconds(): Int {
            return TimeUnit.HOURS.convert(getTimezoneOffsetInMillis().toLong(), TimeUnit.SECONDS).toInt()
        }

        fun getTimezoneOffsetInMillis(): Int {
            val calendar = GregorianCalendar()
            val timeZone = calendar.timeZone

            return timeZone.rawOffset
        }
    }
}
