package pl.llp.aircasting.data.model

import com.google.common.collect.Lists
import pl.llp.aircasting.data.api.response.SessionStreamResponse
import pl.llp.aircasting.data.api.response.SessionStreamWithMeasurementsResponse
import pl.llp.aircasting.data.api.response.search.Sensor
import pl.llp.aircasting.data.local.entity.MeasurementStreamDBObject
import pl.llp.aircasting.data.local.entity.StreamWithLastMeasurementsDBObject
import pl.llp.aircasting.data.local.entity.StreamWithMeasurementsDBObject
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.util.events.NewMeasurementEvent
import pl.llp.aircasting.util.extensions.addHours
import pl.llp.aircasting.util.extensions.calendar
import pl.llp.aircasting.util.helpers.sensor.microphone.MicrophoneDeviceItem
import java.util.*

open class MeasurementStream(
    val sensorPackageName: String,
    val sensorName: String,
    val measurementType: String,
    val measurementShortType: String,
    val unitName: String,
    var unitSymbol: String,
    var thresholdVeryLow: Int,
    var thresholdLow: Int,
    var thresholdMedium: Int,
    var thresholdHigh: Int,
    var thresholdVeryHigh: Int,
    var deleted: Boolean = false,
    private var mMeasurements: List<Measurement> = listOf()
) {
    constructor(measurementEvent: NewMeasurementEvent) : this(
        measurementEvent.sensorPackageName,
        measurementEvent.sensorName,
        measurementEvent.measurementType,
        measurementEvent.measurementShortType,
        measurementEvent.unitName,
        measurementEvent.unitSymbol,
        measurementEvent.thresholdVeryLow,
        measurementEvent.thresholdLow,
        measurementEvent.thresholdMedium,
        measurementEvent.thresholdHigh,
        measurementEvent.thresholdVeryHigh,
        false
    )

    constructor(sensor: Sensor) : this(
        sensor.sensorPackageName ?: sensor.sensorName,
        sensor.sensorName,
        sensor.measurementType,
        sensor.measurementShortType,
        sensor.unitName,
        sensor.unitSymbol,
        sensor.thresholdVeryLow,
        sensor.thresholdLow,
        sensor.thresholdMedium,
        sensor.thresholdHigh,
        sensor.thresholdVeryHigh,
    )

    constructor(sensor: Sensor, sensorThreshold: SensorThreshold) : this(
        sensor.sensorPackageName ?: sensor.sensorName,
        sensor.sensorName,
        sensor.measurementType,
        sensor.measurementShortType,
        sensor.unitName,
        sensor.unitSymbol,
        sensorThreshold.thresholdVeryLow,
        sensorThreshold.thresholdLow,
        sensorThreshold.thresholdMedium,
        sensorThreshold.thresholdHigh,
        sensorThreshold.thresholdVeryHigh,
    )

    constructor(sensor: Sensor, measurements: List<Measurement>?) : this(
        sensor.sensorPackageName ?: sensor.sensorName,
        sensor.sensorName,
        sensor.measurementType,
        sensor.measurementShortType,
        sensor.unitName,
        sensor.unitSymbol,
        sensor.thresholdVeryLow,
        sensor.thresholdLow,
        sensor.thresholdMedium,
        sensor.thresholdHigh,
        sensor.thresholdVeryHigh,
        mMeasurements = measurements ?: listOf()
    )

    constructor(streamDbObject: MeasurementStreamDBObject) : this(
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

    constructor(streamWithMeasurementsDBObject: StreamWithMeasurementsDBObject) : this(
        streamWithMeasurementsDBObject.stream
    ) {
        this.mMeasurements =
            streamWithMeasurementsDBObject.measurements.map { measurementDBObject ->
                Measurement(measurementDBObject)
            }
    }

    constructor(streamWithLastMeasurementsDBObject: StreamWithLastMeasurementsDBObject) :
            this(streamWithLastMeasurementsDBObject.stream) {
        this.mMeasurements =
            streamWithLastMeasurementsDBObject.measurements.map { measurementDBObject ->
                Measurement(measurementDBObject)
            }.sortedWith(compareBy { it.time })
    }

    constructor(sessionStreamResponse: SessionStreamResponse) : this(
        sessionStreamResponse.sensorPackageName,
        sessionStreamResponse.sensorName,
        sessionStreamResponse.measurementType,
        sessionStreamResponse.measurementShortType,
        sessionStreamResponse.unitName,
        sessionStreamResponse.unitSymbol,
        sessionStreamResponse.thresholdVeryLow,
        sessionStreamResponse.thresholdLow,
        sessionStreamResponse.thresholdMedium,
        sessionStreamResponse.thresholdHigh,
        sessionStreamResponse.thresholdVeryHigh,
        sessionStreamResponse.deleted
    )

    constructor(
        sessionStreamWithMeasurementsResponse: SessionStreamWithMeasurementsResponse
    ) : this(sessionStreamWithMeasurementsResponse as SessionStreamResponse) {
        this.mMeasurements =
            sessionStreamWithMeasurementsResponse.measurements.map { measurementResponse ->
                Measurement(measurementResponse)
            }
    }

    var detailedType: String?
    val measurements get() = mMeasurements

    init {
        detailedType = buildDetailedType()
    }

    companion object {
        private const val AIRBEAM_SENSOR_NAME_REGEX = "airbeam"
    }

    fun setMeasurements(measurements: List<Measurement>) {
        mMeasurements = measurements
    }

    fun sensorNameOrder(): Int? {
        return if (sensorName.contains(
                AIRBEAM_SENSOR_NAME_REGEX,
                true
            )
        ) SensorName.fromString(detailedType)?.ordinal
        else 0
    }

    private fun buildDetailedType(): String? {
        return when (sensorPackageName) {
            MicrophoneDeviceItem.DEFAULT_ID -> MicrophoneDeviceItem.DETAILED_TYPE
            else -> {
                val split = sensorName.split("-")
                split.lastOrNull()
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

    fun getMeasurementsForPeriod(amount: Int, divisor: Double): MutableList<Measurement> {
        val frequency = samplingFrequency(divisor)
        return try {
            val measurementsInPeriod = (60 / frequency).toInt() * amount
            getLastMeasurements(measurementsInPeriod)
        } catch (e: IndexOutOfBoundsException) {
            getMeasurementsForPeriod(amount - 1, divisor)
        }
    }

    fun getMeasurementsForTimeSpan(timeSpan: ClosedRange<Date>): List<Measurement> {
        return measurements.filter { it.time in timeSpan }
    }

    fun getLastMeasurements(amount: Int = ActiveSessionMeasurementsRepository.MAX_MEASUREMENTS_PER_STREAM_NUMBER): MutableList<Measurement> {
        // copy the backing list to avoid ConcurrentModificationException
        val allMeasurements = ArrayList(measurements)
        val measurementsSize = allMeasurements.size

        if (amount >= measurementsSize) return allMeasurements

        return allMeasurements.subList(measurementsSize - amount, measurementsSize)
    }

    fun getLast24HoursOfMeasurements(): List<Measurement> {
        val end = measurements.lastOrNull()?.time ?: Date()
        val start = calendar().addHours(end, -24)
        val range = start..end

        return measurements.filter { it.time in range }
    }

    fun getLastMeasurementValue(): Double {
        return lastMeasurement()?.value ?: 0.0
    }

    fun getAvgMeasurement(): Double? {
        if (measurements.isEmpty()) return null
        return calculateSum() / measurements.size
    }

    private fun calculateSum(): Double {
        return measurements.sumOf { it.value }
    }

    fun calculateSum(visibleTimeSpan: ClosedRange<Date>): Double {
        return getMeasurementsForTimeSpan(visibleTimeSpan).sumOf { it.value }
    }

    private fun getFirstMeasurements(amount: Int): List<Measurement?> {
        val allMeasurements = ArrayList(measurements)
        val size = allMeasurements.size
        return if (size > amount) {
            allMeasurements.subList(0, amount - 1)
        } else {
            allMeasurements
        }
    }

    fun lastMeasurementsByAveragingFrequency(amount: Int, threshold: Int): List<Measurement> {
        val filteredMeasurements =
            measurements.filter { measurement -> measurement.averagingFrequency == threshold }
        val measurementsSize = filteredMeasurements.size

        if (amount >= measurementsSize) return filteredMeasurements

        return filteredMeasurements.takeLast(amount)
    }

    fun lastMeasurement(): Measurement? {
        return measurements.lastOrNull()
    }

    fun isMeasurementTypeTemperature(): Boolean {
        return measurementType == "Temperature"
    }

    fun isDetailedTypeCelsius(): Boolean {
        return detailedType == "C"
    }

    override fun hashCode(): Int {
        var result = sensorPackageName.hashCode()
        result = 31 * result + sensorName.hashCode()
        result = 31 * result + measurementType.hashCode()
        result = 31 * result + measurementShortType.hashCode()
        result = 31 * result + unitName.hashCode()
        result = 31 * result + unitSymbol.hashCode()
        result = 31 * result + thresholdVeryLow
        result = 31 * result + thresholdLow
        result = 31 * result + thresholdMedium
        result = 31 * result + thresholdHigh
        result = 31 * result + thresholdVeryHigh
        result = 31 * result + deleted.hashCode()
        result = 31 * result + mMeasurements.hashCode()
        result = 31 * result + (detailedType?.hashCode() ?: 0)
        return result
    }
}
