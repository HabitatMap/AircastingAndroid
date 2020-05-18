package io.lunarlogic.aircasting.sensor

import io.lunarlogic.aircasting.database.data_classes.StreamWithMeasurementsDBObject
import io.lunarlogic.aircasting.events.NewMeasurementEvent

class MeasurementStream(
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
    val thresholdVeryHigh: Int
) {
    constructor(measurementEvent: NewMeasurementEvent): this(
        measurementEvent.packageName,
        measurementEvent.sensorName,
        measurementEvent.measurementType,
        measurementEvent.measurementShortType,
        measurementEvent.unitName,
        measurementEvent.unitSymbol,
        measurementEvent.thresholdVeryLow,
        measurementEvent.thresholdLow,
        measurementEvent.thresholdMedium,
        measurementEvent.thresholdHigh,
        measurementEvent.thresholdVeryHigh
    )

    constructor(streamWithMeasurementsDBObject: StreamWithMeasurementsDBObject): this(
        streamWithMeasurementsDBObject.stream.sensorPackageName,
        streamWithMeasurementsDBObject.stream.sensorName,
        streamWithMeasurementsDBObject.stream.measurementType,
        streamWithMeasurementsDBObject.stream.measurementShortType,
        streamWithMeasurementsDBObject.stream.unitName,
        streamWithMeasurementsDBObject.stream.unitSymbol,
        streamWithMeasurementsDBObject.stream.thresholdVeryLow,
        streamWithMeasurementsDBObject.stream.thresholdLow,
        streamWithMeasurementsDBObject.stream.thresholdMedium,
        streamWithMeasurementsDBObject.stream.thresholdHigh,
        streamWithMeasurementsDBObject.stream.thresholdVeryHigh
    ) {
        this.mMeasurements = streamWithMeasurementsDBObject.measurements.map { measurementDBObject ->
            Measurement(measurementDBObject)
        }
    }

    val detailedType: String

    private var mMeasurements = listOf<Measurement>()
    val measurements get() = mMeasurements


    init {
        detailedType = sensorName.split("-").component2()
    }
}