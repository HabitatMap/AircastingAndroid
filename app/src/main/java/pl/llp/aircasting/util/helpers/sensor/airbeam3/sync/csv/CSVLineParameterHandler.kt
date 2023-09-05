package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv

import pl.llp.aircasting.util.DateConverter
import java.util.Date

class CSVLineParameterHandler {
    companion object {
        private const val AB_DELIMITER = ","

        fun uuidFrom(line: String?): String? {
            line ?: return null

            return lineParameters(line)[CSVSession.AB3LineParameter.UUID.position]
        }

        fun timestampFrom(line: String?): Date? {
            line ?: return null

            val lineParameters = lineParameters(line)
            val dateString =
                "${lineParameters[CSVSession.AB3LineParameter.Date.position]} ${lineParameters[CSVSession.AB3LineParameter.Time.position]}"
            return DateConverter.fromString(
                dateString,
                dateFormat = CSVSession.DATE_FORMAT
            )
        }

        fun lineParameters(line: String): List<String> = line.split(AB_DELIMITER)

        private const val PM_MEASUREMENT_TYPE = "Particulate Matter"
        private const val PM_MEASUREMENT_SHORT_TYPE = "PM"
        private const val PM_UNIT_NAME = "microgram per cubic meter"
        private const val PM_UNIT_SYMBOL = "µg/m³"

        val SUPPORTED_STREAMS = hashMapOf(
            CSVSession.AB3LineParameter.F to CSVMeasurementStream(
                "${CSVMeasurementStream.DEVICE_NAME}-F",
                "Temperature",
                "F",
                "fahrenheit",
                "F",
                15,
                45,
                75,
                105,
                135
            ),
            CSVSession.AB3LineParameter.RH to CSVMeasurementStream(
                "${CSVMeasurementStream.DEVICE_NAME}-RH",
                "Humidity",
                "RH",
                "percent",
                "%",
                0,
                25,
                50,
                75,
                100
            ),
            CSVSession.AB3LineParameter.PM1 to CSVMeasurementStream(
                "${CSVMeasurementStream.DEVICE_NAME}-PM1",
                PM_MEASUREMENT_TYPE,
                PM_MEASUREMENT_SHORT_TYPE,
                PM_UNIT_NAME,
                PM_UNIT_SYMBOL,
                0,
                12,
                35,
                55,
                150
            ),
            CSVSession.AB3LineParameter.PM2_5 to CSVMeasurementStream(
                "${CSVMeasurementStream.DEVICE_NAME}-PM2.5",
                PM_MEASUREMENT_TYPE,
                PM_MEASUREMENT_SHORT_TYPE,
                PM_UNIT_NAME,
                PM_UNIT_SYMBOL,
                0,
                12,
                35,
                55,
                150
            ),
            CSVSession.AB3LineParameter.PM10 to CSVMeasurementStream(
                "${CSVMeasurementStream.DEVICE_NAME}-PM10",
                PM_MEASUREMENT_TYPE,
                PM_MEASUREMENT_SHORT_TYPE,
                PM_UNIT_NAME,
                PM_UNIT_SYMBOL,
                0,
                20,
                50,
                100,
                200
            )
        )

        fun fromHeader(streamLineParameter: CSVSession.AB3LineParameter): CSVMeasurementStream? {
            return SUPPORTED_STREAMS[streamLineParameter]
        }
    }
}