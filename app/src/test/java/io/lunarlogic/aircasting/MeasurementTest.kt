package io.lunarlogic.aircasting

import io.lunarlogic.aircasting.sensor.Measurement
import io.lunarlogic.aircasting.sensor.MeasurementStream
import org.junit.Test

import org.junit.Assert.*
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class MeasurementTest {
    @Test
    fun getLevelTest() {
        val stream = MeasurementStream(
            "AirBeam2",
            "PM2.5",
            "Particulate Matter",
            "PM",
            "micrograms per cubic meter",
            "µg/m³",
            1,
            12,
            35,
            55,
            150
        )

        var measurement = Measurement(0.0, Date())
        assertEquals(Measurement.Level.EXTREMELY_LOW, measurement.getLevel(stream))

        measurement = Measurement(1.0, Date())
        assertEquals(Measurement.Level.LOW, measurement.getLevel(stream))

        measurement = Measurement(5.0, Date())
        assertEquals(Measurement.Level.LOW, measurement.getLevel(stream))

        measurement = Measurement(15.0, Date())
        assertEquals(Measurement.Level.MEDIUM, measurement.getLevel(stream))

        measurement = Measurement(40.0, Date())
        assertEquals(Measurement.Level.HIGH, measurement.getLevel(stream))

        measurement = Measurement(100.0, Date())
        assertEquals(Measurement.Level.VERY_HIGH, measurement.getLevel(stream))

        measurement = Measurement(200.0, Date())
        assertEquals(Measurement.Level.EXTREMELY_HIGH, measurement.getLevel(stream))
    }
}
