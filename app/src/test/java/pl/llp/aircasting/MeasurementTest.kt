package pl.llp.aircasting

import junit.framework.TestCase
import org.junit.Test
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.SensorThreshold
import java.util.*

class MeasurementTest : TestCase() {
    @Test
    fun testGetLevel() {
        val sensorThreshold = SensorThreshold(
            "PM2.5",
            1,
            12,
            35,
            55,
            150
        )

        var measurement = Measurement(0.0, Date())
        assertEquals(Measurement.Level.EXTREMELY_LOW, measurement.getLevel(sensorThreshold))

        measurement = Measurement(1.0, Date())
        assertEquals(Measurement.Level.LOW, measurement.getLevel(sensorThreshold))

        measurement = Measurement(5.0, Date())
        assertEquals(Measurement.Level.LOW, measurement.getLevel(sensorThreshold))

        measurement = Measurement(15.0, Date())
        assertEquals(Measurement.Level.MEDIUM, measurement.getLevel(sensorThreshold))

        measurement = Measurement(40.0, Date())
        assertEquals(Measurement.Level.HIGH, measurement.getLevel(sensorThreshold))

        measurement = Measurement(100.0, Date())
        assertEquals(Measurement.Level.VERY_HIGH, measurement.getLevel(sensorThreshold))

        measurement = Measurement(200.0, Date())
        assertEquals(Measurement.Level.EXTREMELY_HIGH, measurement.getLevel(sensorThreshold))
    }
}
