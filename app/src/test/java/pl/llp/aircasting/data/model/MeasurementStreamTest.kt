package pl.llp.aircasting.data.model

import org.junit.Test
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import pl.llp.aircasting.util.extensions.addHours
import pl.llp.aircasting.util.extensions.calendar

class MeasurementStreamTest {

    @Test
    fun testMeasurementsAreSortedOnCreation() {
        val now = Date()
        val m1 = Measurement(1.0, calendar().addHours(now, -2))
        val m2 = Measurement(2.0, calendar().addHours(now, -1))
        val m3 = Measurement(3.0, now)

        val unsortedMeasurements = listOf(m3, m1, m2)

        val stream = MeasurementStream(
            "package",
            "name",
            "type",
            "shortType",
            "unit",
            "symbol",
            0, 10, 20, 30, 40,
            false,
            unsortedMeasurements
        )

        val sorted = stream.measurements
        assertEquals(3, sorted.size)
        assertEquals(m1.time, sorted[0].time)
        assertEquals(m2.time, sorted[1].time)
        assertEquals(m3.time, sorted[2].time)
    }

    @Test
    fun testMeasurementsAreSortedOnSet() {
        val now = Date()
        val m1 = Measurement(1.0, calendar().addHours(now, -2))
        val m2 = Measurement(2.0, calendar().addHours(now, -1))
        val m3 = Measurement(3.0, now)

        val stream = MeasurementStream(
            "package",
            "name",
            "type",
            "shortType",
            "unit",
            "symbol",
            0, 10, 20, 30, 40,
            false,
            listOf()
        )

        stream.setMeasurements(listOf(m3, m1, m2))

        val sorted = stream.measurements
        assertEquals(3, sorted.size)
        assertEquals(m1.time, sorted[0].time)
        assertEquals(m2.time, sorted[1].time)
        assertEquals(m3.time, sorted[2].time)
    }

    @Test
    fun testGetLast24HoursOfMeasurements() {
        val now = Date()
        val mOld = Measurement(0.0, calendar().addHours(now, -25))
        val m1 = Measurement(1.0, calendar().addHours(now, -2))
        val m2 = Measurement(2.0, calendar().addHours(now, -1))
        val m3 = Measurement(3.0, now)

        // Pass unsorted measurements
        val stream = MeasurementStream(
            "package",
            "name",
            "type",
            "shortType",
            "unit",
            "symbol",
            0, 10, 20, 30, 40,
            false,
            listOf(m3, mOld, m1, m2)
        )

        val last24h = stream.getLast24HoursOfMeasurements()
        assertEquals(3, last24h.size)
        assertTrue(last24h.contains(m1))
        assertTrue(last24h.contains(m2))
        assertTrue(last24h.contains(m3))
        assertTrue(!last24h.contains(mOld))
    }
}
