package pl.llp.aircasting.ui.view.screens.dashboard.charts

import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.anyOrNull
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.utilities.StubData
import java.io.File
import java.io.FileNotFoundException

class ChartAveragesCreatorTest : TestCase() {
    private val creator = ChartAveragesCreator()
    private lateinit var measurements: MutableList<Measurement>

    @Mock
    private lateinit var stream: MeasurementStream

    @Before
    public override fun setUp() {
        super.setUp()

        try {
            val url = javaClass.classLoader?.getResource("HabitatHQ-RH-15-hours-of-measurements.csv")
            measurements = StubData.measurementsFrom(File(url?.path))
        } catch (e: FileNotFoundException) {
        }
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun fixedEntriesHaveCorrectSize() {
        // given
        val entriesCorrectSize = 9
        stream.setMeasurements(measurements)
        Mockito.doReturn(measurements).`when`(stream).getLastMeasurements()

        // when
        val entries = creator.getFixedEntries(stream, anyOrNull())

        // then
        assertEquals(entriesCorrectSize, entries.size)
    }
}