package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv

import pl.llp.aircasting.util.DateConverter
import java.util.Date

class CSVLineParameterHandler {
    companion object {
        private const val AB_DELIMITER = ","

        fun uuidFrom(line: String?): String? {
            line ?: return null

            return lineParameters(line)[AB3LineParameter.UUID.position]
        }

        fun timestampFrom(line: String?): Date? {
            line ?: return null

            val lineParameters = lineParameters(line)
            val dateString =
                "${lineParameters[AB3LineParameter.Date.position]} ${lineParameters[AB3LineParameter.Time.position]}"
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
            AB3LineParameter.F to CSVMeasurementStream(
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
            AB3LineParameter.RH to CSVMeasurementStream(
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
            AB3LineParameter.PM1 to CSVMeasurementStream(
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
            AB3LineParameter.PM2_5 to CSVMeasurementStream(
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
            AB3LineParameter.PM10 to CSVMeasurementStream(
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

        fun fromHeader(streamLineParameter: AB3LineParameter): CSVMeasurementStream? {
            return SUPPORTED_STREAMS[streamLineParameter]
        }
    }
    sealed class ABMiniLineParameter(val position: Int) {
        object UUID : ABMiniLineParameter(0)
        object Date : ABMiniLineParameter(1)
        object Time : ABMiniLineParameter(2)
        object PM1 : ABMiniLineParameter(3)
        object PM2_5 : ABMiniLineParameter(4)
    }

     sealed class AB3LineParameter(val position: Int) {
        object Index : AB3LineParameter(0)
        object UUID : AB3LineParameter(1)
        object Date : AB3LineParameter(2)
        object Time : AB3LineParameter(3)
        object Latitude : AB3LineParameter(4)
        object Longitude : AB3LineParameter(5)
        object F : AB3LineParameter(6)
        object C : AB3LineParameter(7)
        object K : AB3LineParameter(8)
        object RH : AB3LineParameter(9)
        object PM1 : AB3LineParameter(10)
        object PM2_5 : AB3LineParameter(11)
        object PM10 : AB3LineParameter(12)

        companion object {
            fun fromInt(position: Int) = when (position) {
                Index.position -> Index
                UUID.position -> UUID
                Date.position -> Date
                Time.position -> Time
                Latitude.position -> Latitude
                Longitude.position -> Longitude
                F.position -> F
                C.position -> C
                K.position -> K
                RH.position -> RH
                PM1.position -> PM1
                PM2_5.position -> PM2_5
                PM10.position -> PM10
                else -> null
            }
        }
    }
}