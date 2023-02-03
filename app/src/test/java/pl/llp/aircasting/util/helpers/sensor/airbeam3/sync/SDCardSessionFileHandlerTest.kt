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
import pl.llp.aircasting.util.DateConverter
import pl.llp.aircasting.util.extensions.addHours
import pl.llp.aircasting.util.extensions.calendar
import pl.llp.aircasting.util.helpers.services.AveragingService
import pl.llp.aircasting.utilities.StubData
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class SDCardSessionFileHandlerTest {
    private val file = StubData.getFile("SDCardMeasurementsFromSession.csv")
    private val fileLines = file.readLines()
    private val fileLinesCount = fileLines.count()
    private val fileFirstMeasurementTime = CSVSession.timestampFrom(fileLines.first())
    private val fileLastMeasurementTime = CSVSession.timestampFrom(fileLines.last())
    
    private val streamsCount = 5

    @Test
    fun fixed_read_returnsSessionWithCorrectCountOfMeasurements() = runTest {
        val iterator = SDCardSessionFileHandlerFixed(mock())
        val measurementsCountInFile = fileLinesCount * streamsCount

        val csvSession = iterator.handle(file)

        assertEquals(measurementsCountInFile, csvMeasurements(csvSession).count())
    }

    @Test
    fun mobile_read_whenNoMeasurementsArePresentInDB_determinesThresholdBasedOnCSVMeasurementsOnly() =
        runTest {
            val averagingThreshold = AveragingService.getAveragingFrequency(
                fileFirstMeasurementTime,
                fileLastMeasurementTime
            )
            val iterator = SDCardSessionFileHandlerMobile(mock(), mock())
            val measurementsAveragedCountInFile =
                fileMeasurementsCountAfterAveraging(averagingThreshold)

            val csvSession = iterator.handle(file)

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
            val iterator = SDCardSessionFileHandlerMobile(mock(), sessionsRepository)
            val measurementsAveragedCountInFile =
                fileMeasurementsCountAfterAveraging(averagingThreshold)

            val csvSession = iterator.handle(file)

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
            val iterator = SDCardSessionFileHandlerMobile(mock(), sessionsRepository)
            val measurementsAveragedCountInFile =
                fileMeasurementsCountAfterAveraging(averagingThreshold)

            val csvSession = iterator.handle(file)

            assertEquals(measurementsAveragedCountInFile, csvMeasurements(csvSession).count())
        }

    @Test
    fun mobile_read_buildsAveragedMeasurementCorrectlyWithFrequency5() =
        runTest {
            val file = StubData.getFile("10SDCardMeasurementsFromSessionToAverage.csv")
            val fileStartTime = DateConverter.fromString(
                "01/06/2023 11:19:37",
                dateFormat = CSVSession.DATE_FORMAT
            )
            val firstFahrenheitAverage = 72.0
            val firstRHAverage = 64.0
            val firstLatitude = 50.0582475
            val firstLongitude = 19.9261414
            val firstTime = DateConverter.fromString(
                "01/01/2023 11:19:39",
                dateFormat = CSVSession.DATE_FORMAT
            )
            val secondFahrenheitAverage = 71.0
            val secondRHAverage = 43.0
            val secondLatitude = 38.0582475
            val secondLongitude = 18.9261414
            val secondTime = DateConverter.fromString(
                "01/01/2100 11:19:44",
                dateFormat = CSVSession.DATE_FORMAT
            )
            val threeHoursBefore = calendar().addHours(fileStartTime!!, -3)
            val sessionsRepository = mock<SessionsRepository>()
            val dbSession = mock<SessionDBObject> {
                on { startTime } doReturn threeHoursBefore
                on { id } doReturn 1L
            }
            whenever(sessionsRepository.getSessionByUUID(any())).thenReturn(dbSession)
            val iterator = SDCardSessionFileHandlerMobile(mock(), sessionsRepository)

            val csvSession = iterator.handle(file)
            val firstResultFahrenheitAverage =
                csvSession!!.streams[SDCardCSVFileFactory.Header.F.value]!![0]
            val secondResultFahrenheitAverage =
                csvSession.streams[SDCardCSVFileFactory.Header.F.value]!![1]
            val firstResultRHAverage =
                csvSession.streams[SDCardCSVFileFactory.Header.RH.value]!![0]
            val secondResultRHAverage =
                csvSession.streams[SDCardCSVFileFactory.Header.RH.value]!![1]

            assertEquals(firstFahrenheitAverage, firstResultFahrenheitAverage.value)
            assertEquals(secondFahrenheitAverage, secondResultFahrenheitAverage.value)
            assertEquals(firstLatitude, firstResultFahrenheitAverage.latitude)
            assertEquals(secondLatitude, secondResultFahrenheitAverage.latitude)
            assertEquals(firstLongitude, firstResultFahrenheitAverage.longitude)
            assertEquals(secondLongitude, secondResultFahrenheitAverage.longitude)
            assertEquals(firstTime, firstResultFahrenheitAverage.time)
            assertEquals(secondTime, secondResultFahrenheitAverage.time)
            assertEquals(firstRHAverage, firstResultRHAverage.value)
            assertEquals(secondRHAverage, secondResultRHAverage.value)
            assertEquals(firstLatitude, firstResultRHAverage.latitude)
            assertEquals(secondLatitude, secondResultRHAverage.latitude)
            assertEquals(firstLongitude, firstResultRHAverage.longitude)
            assertEquals(secondLongitude, secondResultRHAverage.longitude)
            assertEquals(firstTime, firstResultRHAverage.time)
            assertEquals(secondTime, secondResultRHAverage.time)
        }

    @Test
    fun mobile_read_buildsAveragedMeasurementCorrectlyWithFrequency60() =
        runTest {
            val file = StubData.getFile("60SDCardMeasurementsFromSessionToAverage.csv")
            val fileStartTime = DateConverter.fromString(
                "01/06/2023 11:19:37",
                dateFormat = CSVSession.DATE_FORMAT
            )
            val firstFahrenheitAverage = 72.78333333333333
            val firstRHAverage = 49.5
            val firstLatitude = 78.0582475
            val firstLongitude = 90.9261414
            val firstTime = DateConverter.fromString(
                "01/06/2023 11:20:08",
                dateFormat = CSVSession.DATE_FORMAT
            )
            val tenHoursBefore = calendar().addHours(fileStartTime!!, -10)
            val sessionsRepository = mock<SessionsRepository>()
            val dbSession = mock<SessionDBObject> {
                on { startTime } doReturn tenHoursBefore
                on { id } doReturn 1L
            }
            whenever(sessionsRepository.getSessionByUUID(any())).thenReturn(dbSession)
            val iterator = SDCardSessionFileHandlerMobile(mock(), sessionsRepository)

            val csvSession = iterator.handle(file)
            val firstResultFahrenheitAverage =
                csvSession!!.streams[SDCardCSVFileFactory.Header.F.value]!![0]
            val firstResultRHAverage =
                csvSession.streams[SDCardCSVFileFactory.Header.RH.value]!![0]

            assertEquals(firstFahrenheitAverage, firstResultFahrenheitAverage.value)
            assertEquals(firstLatitude, firstResultFahrenheitAverage.latitude)
            assertEquals(firstLongitude, firstResultFahrenheitAverage.longitude)
            assertEquals(firstTime, firstResultFahrenheitAverage.time)
            assertEquals(firstRHAverage, firstResultRHAverage.value)
            assertEquals(firstLatitude, firstResultRHAverage.latitude)
            assertEquals(firstLongitude, firstResultRHAverage.longitude)
            assertEquals(firstTime, firstResultRHAverage.time)
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