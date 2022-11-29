package pl.llp.aircasting.ui.view.screens.dashboard.charts

import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.utilities.StubData
import java.io.File
import java.io.FileNotFoundException
import kotlin.test.assertEquals

class ChartAveragesCreatorTest {
    private val creator = ChartAveragesCreator()
    private lateinit var measurements: MutableList<Measurement>
    private lateinit var stream: MeasurementStream

    @Before
    fun setUp() {
        try {
            val url =
                javaClass.classLoader?.getResource("HabitatHQ-RH-15-hours-of-measurements.csv")
            measurements = StubData.measurementsFrom(File(url?.path))
        } catch (e: FileNotFoundException) { println(e.stackTrace) }
    }

    @Test
    fun testFixedEntriesHaveCorrectSize() {
        // given
        val entriesCorrectSize = 9
        val timeStampsSetter = mock<SessionChartDataCalculator.TimeStampsSetter>()
        stream = mock()
        whenever(stream.measurements).thenReturn(measurements)

        // when
        val entries = creator.getFixedEntries(stream, timeStampsSetter)

        // then
        assertEquals(entriesCorrectSize, entries.size)
    }
}