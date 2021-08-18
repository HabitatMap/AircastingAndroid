package pl.llp.aircasting.lib

import java.text.SimpleDateFormat
import java.util.*

class DateConverter {
    companion object {
        private var singleton: DateConverter? = null

        fun setup(settings: Settings) {
            if (singleton == null) singleton = DateConverter(settings)
        }

        fun get(): DateConverter {
            return singleton!!
        }
    }

    private var mSettings: Settings? = null
    val DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
    private val DATE_FORMAT = "MM/dd/yy"
    private val HOUR_FORMAT_24 = "HH:mm"
    private val HOUR_FORMAT_12 = "hh:mm a"

    private constructor(settings: Settings) {
        this.mSettings = settings
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

    fun isTheSameDay(startTime: Date, endTime: Date): Boolean {
        val dateFormat = SimpleDateFormat("yyyyMMdd")
        return dateFormat.format(startTime) == dateFormat.format(endTime)
    }

    fun toTimeStringForDisplay(date: Date, timeZone: TimeZone = TimeZone.getDefault()): String {
        if (singleton?.mSettings?.isUsing24HourFormat() == true) {
            return toDateString(date, timeZone, HOUR_FORMAT_24)
        } else {
            return toDateString(date, timeZone, HOUR_FORMAT_12)
        }
    }

    fun toDateStringForDisplay(date: Date, timeZone: TimeZone = TimeZone.getDefault()): String {
        return toDateString(date, timeZone, DATE_FORMAT)
    }

}
