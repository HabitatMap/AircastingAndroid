package pl.llp.aircasting.lib

import pl.llp.aircasting.models.Session
import java.text.SimpleDateFormat
import java.util.*

class DurationStringHelper {

    companion object {

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
}
