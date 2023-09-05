package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv

import pl.llp.aircasting.data.model.MeasurementStream

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
        const val DEVICE_NAME = "AirBeam3"
    }

    fun sensorPackageName(deviceId: String): String {
        return "$DEVICE_NAME-${deviceId}"
    }

    fun toMeasurementStream(deviceId: String): MeasurementStream {
        return MeasurementStream(
            sensorPackageName(deviceId),
            sensorName,
            measurementType,
            measurementShortType,
            unitName,
            unitSymbol,
            thresholdVeryLow,
            thresholdLow,
            thresholdMedium,
            thresholdHigh,
            thresholdVeryHigh
        )
    }
}
