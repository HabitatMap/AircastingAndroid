package pl.llp.aircasting.util

class SearchHelper {
    companion object {
        private val dateConverter = DateConverter.get()

        fun formatTime(time: String = ""): String? =
            DateConverter.fromString(time)?.let { dateConverter?.toTimeStringForDisplay(it) }

        fun formatDate(date: String = ""): String? = DateConverter.fromString(date)
            ?.let { DateConverter.toDateStringForDisplay(it) }

        fun formatType(type: String = ""): String {
            val splitByCapitalLetter = type.split(Regex("(?=[A-Z])"))
            return splitByCapitalLetter[1]
        }

        fun formatSensorName(sensor: String = ""): String {
            val splitByHyphen = sensor.split("-")
            return splitByHyphen[0]
        }
    }
}