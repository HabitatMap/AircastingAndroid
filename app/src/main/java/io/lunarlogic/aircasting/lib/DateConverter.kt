package io.lunarlogic.aircasting.lib

import java.text.SimpleDateFormat
import java.util.*

class DateConverter {
    companion object {
        val DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"

        fun fromUTCString(dateString: String): Date {
            val timeZone = TimeZone.getTimeZone("UTC")
            val parser = SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.getDefault())
            parser.timeZone = timeZone
            return parser.parse(dateString)
        }

        fun toUTCDateString(date: Date): String {
            return toDateString(date, TimeZone.getTimeZone("UTC"))
        }

        fun toLocalDateString(date: Date): String {
            return toDateString(date)
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