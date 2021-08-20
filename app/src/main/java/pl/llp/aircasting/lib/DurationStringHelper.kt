package pl.llp.aircasting.lib

import java.util.*

class DurationStringHelper {
        fun durationString(startTime: Date, endTime: Date?): String {
            val dateConverter = DateConverter.get()
            var durationString = "${dateConverter?.toDateStringForDisplay(startTime)} ${dateConverter?.toTimeStringForDisplay(startTime)}"

            if (endTime == null) return durationString

            if (DateConverter.get()?.isTheSameDay(startTime, endTime) == true) {
                durationString += "-${dateConverter?.toTimeStringForDisplay(endTime)}"
            } else {
                durationString += " - ${dateConverter?.toDateStringForDisplay(endTime)} ${dateConverter?.toTimeStringForDisplay(endTime)}"
            }

            return durationString
        }

}
