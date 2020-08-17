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
        assertEquals(null, measurement.getLevel(stream))

        measurement = Measurement(1.0, Date())
        assertEquals(0, measurement.getLevel(stream))

        measurement = Measurement(5.0, Date())
        assertEquals(0, measurement.getLevel(stream))

        measurement = Measurement(15.0, Date())
        assertEquals(1, measurement.getLevel(stream))

        measurement = Measurement(40.0, Date())
        assertEquals(2, measurement.getLevel(stream))

        measurement = Measurement(100.0, Date())
        assertEquals(3, measurement.getLevel(stream))

        measurement = Measurement(200.0, Date())
        assertEquals(null, measurement.getLevel(stream))
    }
}
