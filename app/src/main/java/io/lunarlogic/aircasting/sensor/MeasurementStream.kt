package io.lunarlogic.aircasting.sensor

import io.lunarlogic.aircasting.database.data_classes.MeasurementStreamDBObject
import io.lunarlogic.aircasting.database.data_classes.StreamWithMeasurementsDBObject
import io.lunarlogic.aircasting.events.NewMeasurementEvent
import io.lunarlogic.aircasting.networking.responses.SessionStreamResponse

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

    constructor(streamDbObject: MeasurementStreamDBObject): this(
        streamDbObject.sensorPackageName,
        streamDbObject.sensorName,
        streamDbObject.measurementType,
        streamDbObject.measurementShortType,
        streamDbObject.unitName,
        streamDbObject.unitSymbol,
        streamDbObject.thresholdVeryLow,
        streamDbObject.thresholdLow,
        streamDbObject.thresholdMedium,
        streamDbObject.thresholdHigh,
        streamDbObject.thresholdVeryHigh
    )

    constructor(streamWithMeasurementsDBObject: StreamWithMeasurementsDBObject):
            this(streamWithMeasurementsDBObject.stream) {
        this.mMeasurements = streamWithMeasurementsDBObject.measurements.map { measurementDBObject ->
            Measurement(measurementDBObject)
        }
    }

    constructor(sessionStreamResponse: SessionStreamResponse): this(
        sessionStreamResponse.sensor_package_name,
        sessionStreamResponse.sensor_name,
        sessionStreamResponse.measurement_type,
        sessionStreamResponse.measurement_short_type,
        sessionStreamResponse.unit_name,
        sessionStreamResponse.unit_symbol,
        sessionStreamResponse.threshold_very_low,
        sessionStreamResponse.threshold_low,
        sessionStreamResponse.threshold_medium,
        sessionStreamResponse.threshold_high,
        sessionStreamResponse.threshold_very_high
    )

    val detailedType: String?

    private var mMeasurements = listOf<Measurement>()
    val measurements get() = mMeasurements


    init {
        val splitted = sensorName.split("-")
        detailedType = splitted.lastOrNull()
    }
}