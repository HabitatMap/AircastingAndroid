package pl.llp.aircasting.lib

import pl.llp.aircasting.models.Session
import java.text.SimpleDateFormat
import java.util.*

class DurationStringHelper {

    companion object {

        fun durationString(session: Session): String {

            var durationString = "${DateConverter.toDateStringForDisplay(session.startTime)} ${DateConverter.toTimeStringForDisplay(session.startTime)}"

            if (session.endTime == null) return durationString

            if (DateConverter.isTheSameDay(session.startTime, session.endTime!!)) {
                durationString += "-${DateConverter.toTimeStringForDisplay(session.endTime!!)}"
            } else {
                durationString += " - ${DateConverter.toDateStringForDisplay(session.endTime!!)} ${DateConverter.toTimeStringForDisplay(session.endTime!!)}"
            }

            return durationString
        }

    }
}
