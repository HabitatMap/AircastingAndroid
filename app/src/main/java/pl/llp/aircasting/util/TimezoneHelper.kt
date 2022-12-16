package pl.llp.aircasting.util

import pl.llp.aircasting.util.extensions.calendar
import java.util.concurrent.TimeUnit

object TimezoneHelper {
    fun getTimezoneOffsetInHours(): Int {
        return TimeUnit.HOURS.convert(getTimezoneOffsetInMillis().toLong(), TimeUnit.MILLISECONDS)
            .toInt()
    }

    fun getTimezoneOffsetInSeconds(): Int {
        return TimeUnit.SECONDS.convert(getTimezoneOffsetInMillis().toLong(), TimeUnit.MILLISECONDS)
            .toInt()
    }

    fun getTimezoneOffsetInMillis(): Int {
        val timeZone = calendar().timeZone

        return timeZone.rawOffset
    }
}
