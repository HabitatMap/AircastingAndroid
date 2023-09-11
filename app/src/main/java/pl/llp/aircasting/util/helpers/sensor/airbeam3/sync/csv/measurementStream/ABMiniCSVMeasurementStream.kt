package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv.measurementStream

class ABMiniCSVMeasurementStream(
    sensor: String,
    measurementType: String,
    measurementShortType: String,
    unitName: String,
    unitSymbol: String,
    thresholdVeryLow: Int,
    thresholdLow: Int,
    thresholdMedium: Int,
    thresholdHigh: Int,
    thresholdVeryHigh: Int,
    override val deviceName: String = "AirBeamMini",
) : CSVMeasurementStream(
    sensor,
    measurementType,
    measurementShortType,
    unitName,
    unitSymbol,
    thresholdVeryLow,
    thresholdLow,
    thresholdMedium,
    thresholdHigh,
    thresholdVeryHigh,
    deviceName
)