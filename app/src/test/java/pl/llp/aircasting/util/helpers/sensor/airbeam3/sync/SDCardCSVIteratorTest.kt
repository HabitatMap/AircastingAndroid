package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import pl.llp.aircasting.data.local.entity.SessionDBObject
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.util.extensions.addHours
import pl.llp.aircasting.util.extensions.calendar
import pl.llp.aircasting.util.helpers.services.AveragingService
import pl.llp.aircasting.utilities.StubData
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class SDCardCSVIteratorTest {
    private val file = StubData.getFile("SDCardMeasurementsFromSession.csv")
    private val fileLines = file.readLines()
    private val fileLinesCount = fileLines.count()
    private val fileFirstMeasurementTime = CSVSession.timestampFrom(fileLines.first())
    private val fileLastMeasurementTime = CSVSession.timestampFrom(fileLines.last())
    
    private val streamsCount = 5

    @Test
    fun fixed_read_returnsSessionWithCorrectCountOfMeasurements() = runTest {
        val iterator = SDCardSessionFileReaderFixed(mock())
        val measurementsCountInFile = fileLinesCount * streamsCount

        val csvSession = iterator.read(file)

        assertEquals(measurementsCountInFile, csvMeasurements(csvSession).count())
    }

    @Test
    fun mobile_read_whenNoMeasurementsArePresentInDB_determinesThresholdBasedOnCSVMeasurementsOnly() =
        runTest {
            val averagingThreshold = AveragingService.getAveragingFrequency(
                fileFirstMeasurementTime,
                fileLastMeasurementTime
            )
            val iterator = SDCardSessionFileReaderMobile(mock(), mock())
            val measurementsAveragedCountInFile =
                fileMeasurementsCountAfterAveraging(averagingThreshold)

            val csvSession = iterator.read(file)

            assertEquals(measurementsAveragedCountInFile, csvMeasurements(csvSession).count())
        }

    @Test
    fun mobile_read_averages2hoursOfMeasurementsCorrectly() =
        runTest {
            val threeHoursBefore = calendar().addHours(fileFirstMeasurementTime!!, -3)
            val sessionsRepository = mock<SessionsRepository>()
            val dbSession = mock<SessionDBObject> {
                on { startTime } doReturn threeHoursBefore
                on { id } doReturn 1L
            }
            whenever(sessionsRepository.getSessionByUUID(any())).thenReturn(dbSession)
            val averagingThreshold = AveragingService.getAveragingFrequency(
                threeHoursBefore,
                fileLastMeasurementTime
            )
            val iterator = SDCardSessionFileReaderMobile(mock(), sessionsRepository)
            val measurementsAveragedCountInFile =
                fileMeasurementsCountAfterAveraging(averagingThreshold)

            val csvSession = iterator.read(file)

            assertEquals(measurementsAveragedCountInFile, csvMeasurements(csvSession).count())
        }

    @Test
    fun mobile_read_averages10hoursOfMeasurementsCorrectly() =
        runTest {
            val tenHoursBefore = calendar().addHours(fileFirstMeasurementTime!!, -10)
            val sessionsRepository = mock<SessionsRepository>()
            val dbSession = mock<SessionDBObject> {
                on { startTime } doReturn tenHoursBefore
                on { id } doReturn 1L
            }
            whenever(sessionsRepository.getSessionByUUID(any())).thenReturn(dbSession)
            val averagingThreshold = AveragingService.getAveragingFrequency(
                tenHoursBefore,
                fileLastMeasurementTime
            )
            val iterator = SDCardSessionFileReaderMobile(mock(), sessionsRepository)
            val measurementsAveragedCountInFile =
                fileMeasurementsCountAfterAveraging(averagingThreshold)

            val csvSession = iterator.read(file)

            assertEquals(measurementsAveragedCountInFile, csvMeasurements(csvSession).count())
        }

    private fun fileMeasurementsCountAfterAveraging(averagingThreshold: Int) =
        fileLinesCount / averagingThreshold * streamsCount

    private fun csvMeasurements(csvSession: CSVSession?): List<CSVMeasurement> {
        var measurements: List<CSVMeasurement> = arrayListOf()
        csvSession?.streams?.values?.forEach {
            measurements = measurements.plus(it)
        }
        return measurements
    }
}