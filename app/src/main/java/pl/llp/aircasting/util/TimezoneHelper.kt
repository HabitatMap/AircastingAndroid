package pl.llp.aircasting.util

import pl.llp.aircasting.util.extensions.calendar
import java.util.concurrent.TimeUnit

object TimezoneHelper {
    fun getTimezoneOffsetInHours(): Int {
        return TimeUnit.HOURS.convert(getTimezoneOffsetInMillis(), TimeUnit.MILLISECONDS)
            .toInt()
    }

    fun getTimezoneOffsetInSeconds(): Int {
        return TimeUnit.SECONDS.convert(getTimezoneOffsetInMillis(), TimeUnit.MILLISECONDS)
            .toInt()
    }

    private fun getTimezoneOffsetInMillis(): Long {
        val timeZone = calendar().timeZone

        return timeZone.rawOffset.toLong()
    }
}
