package pl.llp.aircasting.util

import java.util.*

class DurationStringHelper {
        fun durationString(startTime: Date, endTime: Date?): String {
            val dateConverter = DateConverter.get()
            var durationString = "${DateConverter.toDateStringForDisplay(startTime)} ${dateConverter?.toTimeStringForDisplay(startTime)}"

            if (endTime == null) return durationString

            durationString += if (DateConverter.isTheSameDay(startTime, endTime)) {
                "-${dateConverter?.toTimeStringForDisplay(endTime)}"
            } else {
                " - ${DateConverter.toDateStringForDisplay(endTime)} ${dateConverter?.toTimeStringForDisplay(endTime)}"
            }

            return durationString
        }

}
