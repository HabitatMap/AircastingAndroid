package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv.measurementStream

import org.junit.Test
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv.lineParameter.CSVLineParameterHandler
import kotlin.test.assertEquals

internal class ABMiniCSVMeasurementStreamTest {
    private val stream = ABMiniCSVMeasurementStream(
        "PM1",
        CSVLineParameterHandler.PM_MEASUREMENT_TYPE,
        CSVLineParameterHandler.PM_MEASUREMENT_SHORT_TYPE,
        CSVLineParameterHandler.PM_UNIT_NAME,
        CSVLineParameterHandler.PM_UNIT_SYMBOL,
        0,
        12,
        35,
        55,
        150
    )
    private val deviceId = "123c123caa123"

    private val deviceName = "AirBeamMini"

    @Test
    fun getSensorName() {
        assertEquals("$deviceName-PM1", stream.sensorName)
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