package pl.llp.aircasting.lib

import java.util.*

class DurationStringHelper {
        fun durationString(startTime: Date, endTime: Date?): String {
            var durationString = "${DateConverter.get()?.toDateStringForDisplay(startTime)} ${DateConverter.get()?.toTimeStringForDisplay(startTime)}"

            if (endTime == null) return durationString

            if (DateConverter.get()?.isTheSameDay(startTime, endTime) == true) {
                durationString += "-${DateConverter.get()?.toTimeStringForDisplay(endTime)}"
            } else {
                durationString += " - ${DateConverter.get()?.toDateStringForDisplay(endTime)} ${DateConverter.get()?.toTimeStringForDisplay(endTime)}"
            }

            return durationString
        }

}
