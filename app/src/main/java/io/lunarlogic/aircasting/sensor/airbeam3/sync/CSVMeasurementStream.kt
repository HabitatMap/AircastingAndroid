package io.lunarlogic.aircasting.sensor.airbeam3.sync

class CSVMeasurementStream(
    val sensorName: String,
    val measurementType: String,
    val measurementShortType: String,
    val unitName: String,
    val unitSymbol: String,
    val thresholdVeryLow: Int,
    val thresholdLow: Int,
    val thresholdMedium: Int,
    val thresholdHigh: Int,
    val thresholdVeryHigh: Int
) {
    companion object {
        private const val DEVICE_NAME = "AirBeam3"
        private const val PM_MEASUREMENT_TYPE = "Particulate Matter"
        private const val PM_MEASUREMENT_SHORT_TYPE = "PM"
        private const val PM_UNIT_NAME = "micrograms per cubic meter"
        private const val PM_UNIT_SYMBOL = "µg/m³"

        val SUPPORTED_STREAMS = hashMapOf(
            SDCardCSVFileFactory.Header.F to CSVMeasurementStream(
                "$DEVICE_NAME-F",
                "Temperature",
                "F",
                "degrees Fahrenheit",
                "F",
                15,
                45,
                75,
                100,
                135
            ),
            SDCardCSVFileFactory.Header.RH to CSVMeasurementStream(
                "$DEVICE_NAME-RH",
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
            SDCardCSVFileFactory.Header.PM1 to CSVMeasurementStream(
                "$DEVICE_NAME-PM1",
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
            SDCardCSVFileFactory.Header.PM2_5 to CSVMeasurementStream(
                "$DEVICE_NAME-PM2.5",
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
            SDCardCSVFileFactory.Header.PM10 to CSVMeasurementStream(
                "$DEVICE_NAME-PM10",
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

        fun fromHeader(streamHeader: SDCardCSVFileFactory.Header): CSVMeasurementStream? {
            return SUPPORTED_STREAMS[streamHeader]
        }
    }

    fun sensorPackageName(deviceId: String): String {
        return "$DEVICE_NAME:${deviceId}"
    }
}
