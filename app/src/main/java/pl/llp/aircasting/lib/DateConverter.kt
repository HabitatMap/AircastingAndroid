package pl.llp.aircasting.lib

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DateConverter {
    companion object {
        val singleton: DateConverter? = null
        var mSettings: Settings? = null

        val DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
        private val DATE_FORMAT_24 = "HH:mm"
        private val DATE_FORMAT_12 = "hh:mm a"

        fun setup(settings: Settings) {
            if (singleton == null) DateConverter()
            mSettings = settings
        }

        fun createToDateString(date: Date, timeZone: TimeZone = TimeZone.getDefault(), dateFormat: String = DEFAULT_DATE_FORMAT): String {
            if (mSettings?.isUsing24HourFormat() == true) {
                return toDateString(date, timeZone, DATE_FORMAT_24)
            } else {
                return toDateString(date, timeZone, DATE_FORMAT_12)
            }
        }

        fun fromString(dateString: String, dateFormat: String = DEFAULT_DATE_FORMAT): Date? {
            val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
            parser.timeZone = TimeZone.getDefault()
            return parser.parse(dateString)
        }

        fun toDateString(
            date: Date,
            timeZone: TimeZone = TimeZone.getDefault(),
            dateFormat: String = DEFAULT_DATE_FORMAT
        ): String {
            val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
            formatter.timeZone = timeZone
            return formatter.format(date)
        }
    }
}
