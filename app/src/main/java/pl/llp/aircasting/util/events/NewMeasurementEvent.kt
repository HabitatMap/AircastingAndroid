package pl.llp.aircasting.util.events


class NewMeasurementEvent(
    val sensorPackageName: String,
    val sensorName: String,
    val measurementType: String,
    val measurementShortType: String,
    val unitName: String,
    val unitSymbol: String,
    val thresholdVeryLow: Int,
    val thresholdLow: Int,
    val thresholdMedium: Int,
    val thresholdHigh: Int,
    val thresholdVeryHigh: Int,
    val measuredValue: Double
) {
    val deviceId get(): String? = sensorPackageName.split(':').lastOrNull()
}
