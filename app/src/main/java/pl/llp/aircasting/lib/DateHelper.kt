package pl.llp.aircasting.lib

import pl.llp.aircasting.models.Session
import java.text.SimpleDateFormat
import java.util.*

class DateHelper {

    companion object {

        private val DATE_FORMAT = "MM/dd/yy"
        private val HOUR_FORMAT_24 = "HH:mm"
        private val HOUR_FORMAT_12 = "hh:mm a"

        private var singleton: DateHelper? = null
        private var mSettings: Settings? = null

        fun setup(settings: Settings) {
            if (singleton == null) singleton = DateHelper()
            mSettings = settings
        }

        fun createDurationString(session: Session): String {
            if (mSettings?.isUsing24HourFormat() == true) {
                return durationString(session)
            } else {
                return durationString12HourFormat(session)
            }
        }

        fun durationString(session: Session): String {
            val dateFormatter = dateTimeFormatter(DATE_FORMAT)
            val hourFormatter = dateTimeFormatter(HOUR_FORMAT_24)

            var durationString = "${dateFormatter.format(session.startTime)} ${hourFormatter.format(session.startTime)}"

            if (session.endTime == null) return durationString

            if (isTheSameDay(session.startTime, session.endTime!!)) {
                durationString += "-${hourFormatter.format(session.endTime)}"
            } else {
                durationString += " - ${dateFormatter.format(session.endTime)} ${hourFormatter.format(session.endTime)}"
            }

            return durationString
        }

        fun durationString12HourFormat(session: Session): String {
            val dateFormatter = dateTimeFormatter(DATE_FORMAT)
            val hourFormatter = dateTimeFormatter(HOUR_FORMAT_12)

            var durationString = "${dateFormatter.format(session.startTime)} ${hourFormatter.format(session.startTime)}"

            if (session.endTime == null) return durationString

            if (isTheSameDay(session.startTime, session.endTime!!)) {
                durationString += "-${hourFormatter.format(session.endTime)}"
            } else {
                durationString += " - ${dateFormatter.format(session.endTime)} ${hourFormatter.format(session.endTime)}"
            }

            return durationString
        }

        private fun dateTimeFormatter(dateTimeFormat: String): SimpleDateFormat {
            val formatter = SimpleDateFormat(dateTimeFormat, Locale.getDefault())
            formatter.timeZone = TimeZone.getDefault()
            return formatter
        }

        fun isTheSameDay(startTime: Date, endTime: Date): Boolean {
            val dateFormat = SimpleDateFormat("yyyyMMdd")
            return dateFormat.format(startTime) == dateFormat.format(endTime)
        }
    }
}
