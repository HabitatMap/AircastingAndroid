package pl.llp.aircasting

import pl.llp.aircasting.lib.DateConverter
import pl.llp.aircasting.networking.services.LastMeasurementSyncCalculator
import org.junit.Assert
import org.junit.Test

class LastMeasurementSyncCalculatorTest {
    val sessionEndTime = DateConverter.fromString("1987-07-23T11:00:00")!!

    @Test
    fun lastMeasurementTimeBlankTest() {
        Assert.assertEquals(
            DateConverter.fromString("1987-07-22T11:00:00")!!,
            LastMeasurementSyncCalculator.calculate(sessionEndTime, null)
        )
    }

    @Test
    fun lastMeasurementTimePresentAndInThe24HoursTimeFrameTest() {
        val lastMeasurementTime = DateConverter.fromString("1987-07-22T17:00:00")!!
        Assert.assertEquals(
            DateConverter.fromString("1987-07-22T17:00:00")!!,
            LastMeasurementSyncCalculator.calculate(sessionEndTime, lastMeasurementTime)
        )
    }

    @Test
    fun lastMeasurementTimePresentButOlderThan24HoursTimeFrameTest() {
        val lastMeasurementTime = DateConverter.fromString("1987-07-21T11:00:00")!!
        Assert.assertEquals(
            DateConverter.fromString("1987-07-22T11:00:00")!!,
            LastMeasurementSyncCalculator.calculate(sessionEndTime, lastMeasurementTime)
        )
    }
}
