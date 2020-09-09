package io.lunarlogic.aircasting

import io.lunarlogic.aircasting.lib.DateConverter
import io.lunarlogic.aircasting.networking.services.LastMeasurementSyncCalculator
import io.lunarlogic.aircasting.sensor.Session
import org.junit.Assert
import org.junit.Test
import kotlin.collections.ArrayList

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
