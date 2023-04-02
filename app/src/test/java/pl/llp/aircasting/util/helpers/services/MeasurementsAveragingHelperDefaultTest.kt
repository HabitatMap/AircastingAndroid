package pl.llp.aircasting.util.helpers.services


import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import pl.llp.aircasting.utilities.StubData
import java.util.*
import kotlin.math.round
import kotlin.test.assertEquals

@RunWith(MockitoJUnitRunner::class)
internal class MeasurementsAveragingHelperDefaultTest {
    private lateinit var measurementsAveragingHelper: MeasurementsAveragingHelperDefault

    @Before
    fun setup() {
        measurementsAveragingHelper = MeasurementsAveragingHelperDefault()
    }

    @Test
    fun `averageMeasurements performs second threshold averaging correctly`() {
        runBlocking {
            // Arrange
            val measurements: List<AverageableMeasurement> =
                StubData.dbMeasurementsFrom("60MeasurementsRHwithAveragingFrequency60.csv")
            val startTime = Date(1649310342000L)
            val averagingWindow = AveragingWindow.SECOND
            val expectedAveragedValue = 59.0

            // Act
            measurementsAveragingHelper.averageMeasurements(
                measurements,
                startTime,
                averagingWindow
            ) { measurement, _ ->
                // Assert
                assertEquals(expectedAveragedValue, measurement.value)
            }
        }
    }

    @Test
    fun `averageMeasurements performs first threshold averaging correctly`() {
        runBlocking {
            // Arrange
            val measurements: List<AverageableMeasurement> =
                StubData.dbMeasurementsFrom("5MeasurementsRHwithAveragingFrequency5.csv")
            val startTime = Date(1649310342000L)
            val averagingWindow = AveragingWindow.FIRST
            val expectedAveragedValue = round(59.9)

            // Act
            measurementsAveragingHelper.averageMeasurements(
                measurements,
                startTime,
                averagingWindow
            ) { measurement, _ ->
                // Assert
                assertEquals(expectedAveragedValue, measurement.value)
            }
        }
    }

    @Test
    fun `test calculateAveragingWindow`() {
        // Arrange
        val startTime = System.currentTimeMillis()
        val oneHourPassed = startTime + 1 * 60 * 60 * 1000
        val threeHoursPassed = startTime + 3 * 60 * 60 * 1000
        val tenHoursPassed = startTime + 10 * 60 * 60 * 1000

        // Act
        val resultOneHourPassed =
            measurementsAveragingHelper.calculateAveragingWindow(startTime, oneHourPassed)
        val resultThreeHoursPassed =
            measurementsAveragingHelper.calculateAveragingWindow(startTime, threeHoursPassed)
        val resultTenHoursPassed =
            measurementsAveragingHelper.calculateAveragingWindow(startTime, tenHoursPassed)

        // Assert
        assertEquals(AveragingWindow.ZERO, resultOneHourPassed)
        assertEquals(AveragingWindow.FIRST, resultThreeHoursPassed)
        assertEquals(AveragingWindow.SECOND, resultTenHoursPassed)
    }
}
