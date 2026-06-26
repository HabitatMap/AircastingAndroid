package pl.llp.aircasting.util

import java.util.*

class DurationStringHelper {
        fun durationString(startTime: Date, endTime: Date?, timeZone: TimeZone = TimeZone.getDefault()): String {
            val dateConverter = DateConverter.get()
            var durationString = "${DateConverter.toDateStringForDisplay(startTime, timeZone)} ${dateConverter?.toTimeStringForDisplay(startTime, timeZone)}"

            if (endTime == null) return durationString

            durationString += if (DateConverter.isTheSameDay(startTime, endTime, timeZone)) {
                "-${dateConverter?.toTimeStringForDisplay(endTime, timeZone)}"
            } else {
                " - ${DateConverter.toDateStringForDisplay(endTime, timeZone)} ${dateConverter?.toTimeStringForDisplay(endTime, timeZone)}"
            }

            return durationString
        }

}
