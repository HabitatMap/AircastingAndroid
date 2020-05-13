package io.lunarlogic.aircasting.sensor

import io.lunarlogic.aircasting.events.NewMeasurementEvent

class MeasurementStream {
    constructor(measurementEvent: NewMeasurementEvent) {
        this.sensorPackageName = measurementEvent.packageName
        this.sensorName = measurementEvent.sensorName
        this.measurementType = measurementEvent.measurementType
        this.measurementShortType = measurementEvent.measurementShortType
        this.unitName = measurementEvent.unitName
        this.unitSymbol = measurementEvent.unitSymbol
        this.thresholdVeryLow = measurementEvent.thresholdVeryLow
        this.thresholdLow = measurementEvent.thresholdLow
        this.thresholdMedium = measurementEvent.thresholdMedium
        this.thresholdHigh = measurementEvent.thresholdHigh
        this.thresholdVeryHigh = measurementEvent.thresholdVeryHigh
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