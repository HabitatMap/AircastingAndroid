package pl.llp.aircasting.lib

import java.util.*

class DurationStringHelper {
        fun durationString(startTime: Date?, endTime: Date?): String {
            var durationString = "${DateConverter.toDateStringForDisplay(startTime!!)} ${DateConverter.toTimeStringForDisplay(startTime)}"

            if (endTime == null) return durationString

            if (DateConverter.isTheSameDay(startTime, endTime)) {
                durationString += "-${DateConverter.toTimeStringForDisplay(endTime)}"
            } else {
                durationString += " - ${DateConverter.toDateStringForDisplay(endTime)} ${DateConverter.toTimeStringForDisplay(endTime)}"
            }

            return durationString
        }

}
