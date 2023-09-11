package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv.measurementStream

import org.junit.Test
import pl.llp.aircasting.data.model.MeasurementStream
import kotlin.test.assertEquals

internal class AB3CSVMeasurementStreamTest {
    private val stream = AB3CSVMeasurementStream(
        "F",
        "Temperature",
        "F",
        "fahrenheit",
        "F",
        15,
        45,
        75,
        105,
        135
    )
    private val deviceId = "123c123caa123"

    private val deviceName = "AirBeam3"

    @Test
    fun getSensorName() {
        assertEquals("$deviceName-F", stream.sensorName)
    }

    @Test
    fun sensorPackageName() {
        assertEquals("$deviceName-$deviceId", stream.sensorPackageName(deviceId))
    }

    @Test
    fun toMeasurementStream() {
        stream.apply {
            val sensorPackageName = "$deviceName-$deviceId"
            val measurementStream = MeasurementStream(
                sensorPackageName,
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
            assertEquals(measurementStream.sensorPackageName, stream.sensorPackageName(deviceId))
            assertEquals(measurementStream.measurementType, stream.measurementType)
            assertEquals(measurementStream.measurementShortType, stream.measurementShortType)
            assertEquals(measurementStream.unitName, stream.unitName)
            assertEquals(measurementStream.unitSymbol, stream.unitSymbol)
            assertEquals(measurementStream.thresholdVeryLow, stream.thresholdVeryLow)
            assertEquals(measurementStream.thresholdLow, stream.thresholdLow)
            assertEquals(measurementStream.thresholdMedium, stream.thresholdMedium)
            assertEquals(measurementStream.thresholdHigh, stream.thresholdHigh)
            assertEquals(measurementStream.thresholdVeryHigh, stream.thresholdVeryHigh)
        }
    }
}