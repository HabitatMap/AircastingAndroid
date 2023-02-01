package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import org.junit.Test
import org.mockito.kotlin.mock
import pl.llp.aircasting.utilities.StubData
import kotlin.test.assertEquals

internal class SDCardCSVIteratorFixedTest {
    @Test
    fun read_returnsSessionWithCorrectCountOfMeasurements() {
        val iterator = SDCardCSVIteratorFixed(mock())
        val file = StubData.getFile("SDCardMeasurementsFromSession.csv")
        val measurementsCountInFile = file.readLines().count() * 5

        val csvSession = iterator.read(file)
        var measurements: List<CSVMeasurement> = arrayListOf()
        csvSession?.streams?.values?.forEach {
            measurements = measurements.plus(it)
        }

        assertEquals(measurementsCountInFile, measurements.count())
    }
}