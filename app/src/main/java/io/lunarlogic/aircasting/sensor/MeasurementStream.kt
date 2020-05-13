package io.lunarlogic.aircasting.sensor

class MeasurementStream {
    constructor(measurement: Measurement) {
        this.sensorPackageName = measurement.packageName
        this.sensorName = measurement.sensorName
        this.measurementType = measurement.measurementType
        this.measurementShortType = measurement.measurementShortType
        this.unitName = measurement.unitName
        this.unitSymbol = measurement.unitSymbol
        this.thresholdVeryLow = measurement.thresholdVeryLow
        this.thresholdLow = measurement.thresholdLow
        this.thresholdMedium = measurement.thresholdMedium
        this.thresholdHigh = measurement.thresholdHigh
        this.thresholdVeryHigh = measurement.thresholdVeryHigh
    }

    val sensorPackageName: String
    val sensorName: String?
    val measurementType: String?
    val measurementShortType: String?
    val unitName: String?
    val unitSymbol: String?
    val thresholdVeryLow: Int?
    val thresholdLow: Int?
    val thresholdMedium: Int?
    val thresholdHigh: Int?
    val thresholdVeryHigh: Int?
    val measurements = arrayListOf<Measurement>()


    fun addMeasurement(measurement: Measurement) {
        measurements.add(measurement)
    }
}