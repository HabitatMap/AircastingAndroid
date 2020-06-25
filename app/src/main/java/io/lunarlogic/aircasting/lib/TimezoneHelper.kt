package io.lunarlogic.aircasting.lib

import java.util.*
import java.util.concurrent.TimeUnit

class TimezoneHelper {
    companion object {
        fun getTimezoneOffsetInHours(): Int {
            val calendar = GregorianCalendar()
            val timeZone = calendar.timeZone
            val GMTOffset = timeZone.rawOffset

            return TimeUnit.HOURS.convert(GMTOffset.toLong(), TimeUnit.MILLISECONDS).toInt()
        }
    }
}