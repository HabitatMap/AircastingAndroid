package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.measurementStream

import pl.llp.aircasting.data.model.MeasurementStream

abstract class CSVMeasurementStream(
    val sensor: String,
    val measurementType: String,
    val measurementShortType: String,
    val unitName: String,
    val unitSymbol: String,
    val thresholdVeryLow: Int,
    val thresholdLow: Int,
    val thresholdMedium: Int,
    val thresholdHigh: Int,
    val thresholdVeryHigh: Int,
    open val deviceName: String,
) {

    val sensorName: String get() = "$deviceName-$sensor"
    fun sensorPackageName(deviceId: String) = "$deviceName-$deviceId"

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
