package io.lunarlogic.aircasting.models

import com.google.common.collect.Lists
import io.lunarlogic.aircasting.database.data_classes.MeasurementStreamDBObject
import io.lunarlogic.aircasting.database.data_classes.StreamWithMeasurementsDBObject
import io.lunarlogic.aircasting.events.NewMeasurementEvent
import io.lunarlogic.aircasting.networking.responses.SessionStreamResponse
import io.lunarlogic.aircasting.networking.responses.SessionStreamWithMeasurementsResponse
import io.lunarlogic.aircasting.sensor.microphone.MicrophoneDeviceItem
import java.util.*

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
    val thresholdVeryHigh: Int,
    var deleted: Boolean = false
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
        streamDbObject.thresholdVeryHigh,
        streamDbObject.deleted
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

    constructor(
        sessionStreamWithMeasurementsResponse: SessionStreamWithMeasurementsResponse
    ): this(sessionStreamWithMeasurementsResponse as SessionStreamResponse) {
        this.mMeasurements = sessionStreamWithMeasurementsResponse.measurements.map { measurementResponse ->
            Measurement(measurementResponse)
        }
    }

    val detailedType: String?

    private var mMeasurements = listOf<Measurement>()
    val measurements get() = mMeasurements


    companion object {
        private val AIRBEAM_SENSOR_NAME_REGEX = "airbeam"
    }

    init {
        detailedType = buildDetailedType()
    }

    enum class AirBeamSensorName(val detailedType: String) {
        F("F"),
        PM("PM"),
        PM1("PM1"),
        PM2_5("PM2.5"),
        PM10("PM10"),
        RH("RH");

        companion object {
            fun fromString(detailedType: String?) = values().firstOrNull { it.detailedType == detailedType }
        }
    }

    fun sensorNameOrder(): Int? {
        return if (sensorName.contains(AIRBEAM_SENSOR_NAME_REGEX, true)) {
            AirBeamSensorName.fromString(detailedType)?.ordinal
        } else {
            0
        }
    }


    private fun buildDetailedType(): String? {
        when (sensorPackageName) {
            MicrophoneDeviceItem.DEFAULT_ID -> return MicrophoneDeviceItem.DETAILED_TYPE
            else -> {
                val split = sensorName.split("-")
                return split.lastOrNull()
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is MeasurementStream) return false
        return detailedType == other.detailedType
    }

    fun samplingFrequency(divisor: Double): Double {
        var deltaSum = 0.0
        val sample: ArrayList<Measurement> = Lists.newArrayList(getFirstMeasurements(10))
        for (i in 0 until sample.size - 1) {
            val delta: Double = (sample[i + 1].time.time - sample[i].time.time).toDouble()
            deltaSum += delta
        }
        return deltaSum / divisor
    }

    fun getMeasurementsForPeriod(amount: Int, divisor: Double): MutableList<Measurement>? {
        val frequency = samplingFrequency(divisor)
        return try {
            val measurementsInPeriod = (60 / frequency).toInt() * amount
            getLastMeasurements(measurementsInPeriod)
        } catch (e: IndexOutOfBoundsException) {
            getMeasurementsForPeriod(amount - 1, divisor)
        }
    }

    fun getMeasurementsForTimeSpan(timeSpan: ClosedRange<Date>): List<Measurement> {
        return measurements.filter { it.time in timeSpan}
    }

    fun getLastMeasurements(amount: Int): MutableList<Measurement>? {
        // copy the backing list to avoid ConcurrentModificationException
        val allMeasurements = ArrayList<Measurement>(measurements)

        val measurementsSize = allMeasurements.size

        if (amount >= measurementsSize) return allMeasurements
        
        return allMeasurements.subList(measurementsSize - amount, measurementsSize)
    }

    fun getAvgMeasurement(): Double? {
        if(measurements.isEmpty()) return null
        return calculateSum() / measurements.size
    }

    fun calculateSum(): Double {
        return measurements.sumByDouble { it.value }
    }

    fun calculateSum(visibleTimeSpan: ClosedRange<Date>): Double {
        return getMeasurementsForTimeSpan(visibleTimeSpan).sumByDouble { it.value }
    }

    private fun getFirstMeasurements(amount: Int): List<Measurement?>? {
        val allMeasurements = ArrayList<Measurement?>(measurements)
        val size = allMeasurements.size
        return if (size > amount) {
            allMeasurements.subList(0, amount - 1)
        } else {
            allMeasurements
        }
    }
}
