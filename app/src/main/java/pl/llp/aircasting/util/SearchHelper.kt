package pl.llp.aircasting.util

import java.util.TimeZone

class SearchHelper {
    companion object {
        private val dateConverter = DateConverter.get()
        private val UTC = TimeZone.getTimeZone("UTC")

        fun formatTime(time: String): String? = DateConverter.fromString(time, UTC)
            ?.let { dateConverter?.toTimeStringForDisplay(it, UTC) }

        fun formatDate(date: String): String? = DateConverter.fromString(date, UTC)
            ?.let { DateConverter.toDateStringForDisplay(it, UTC) }

        fun formatType(type: String): String {
            val splitByCapitalLetter = type.split(Regex("(?=[A-Z])"))
            return splitByCapitalLetter[1]
        }

        fun formatSensorName(sensor: String): String {
            val splitByHyphen = sensor.split("-")
            return splitByHyphen[0]
        }
    }
}