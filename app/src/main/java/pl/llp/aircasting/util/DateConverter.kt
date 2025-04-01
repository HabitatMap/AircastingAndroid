package pl.llp.aircasting.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DateConverter private constructor(settings: Settings) {
    companion object {
        private var singleton: DateConverter? = null
        private const val DATE_FORMAT = "MM/dd/yy"
        const val HOUR_FORMAT_24 = "HH:mm"
        const val HOUR_FORMAT_12 = "hh:mm a"
        private const val DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"

        fun setup(settings: Settings) {
            if (singleton == null) singleton = DateConverter(settings)
        }

        fun get(): DateConverter? {
            return singleton
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

        fun fromString(dateString: String, timeZone: TimeZone = TimeZone.getDefault(), dateFormat: String = DEFAULT_DATE_FORMAT): Date? {
            val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
            parser.timeZone = timeZone
            return parser.parse(dateString)
        }

        fun isTheSameDay(startTime: Date, endTime: Date): Boolean {
            val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            return dateFormat.format(startTime) == dateFormat.format(endTime)
        }

        fun toDateStringForDisplay(date: Date, timeZone: TimeZone = TimeZone.getDefault()): String {
            return toDateString(date, timeZone, DATE_FORMAT)
        }
    }

    private var mSettings: Settings? = settings

    fun toTimeStringForDisplay(date: Date, timeZone: TimeZone = TimeZone.getDefault()): String {
        return if (mSettings?.isUsing24HourFormat() == true) {
            toDateString(date, timeZone, HOUR_FORMAT_24)
        } else {
            toDateString(date, timeZone, HOUR_FORMAT_12)
        }
    }
}
