package pl.llp.aircasting.lib

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DateConverter {
    companion object {
        val DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"

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
