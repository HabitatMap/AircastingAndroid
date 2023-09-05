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
import pl.llp.aircasting.util.extensions.addSeconds
import pl.llp.aircasting.util.extensions.calendar
import pl.llp.aircasting.util.helpers.services.AveragingWindow
import pl.llp.aircasting.util.helpers.services.MeasurementsAveragingHelperDefault
import pl.llp.aircasting.utilities.StubData
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class SDCardSessionFileHandlerTest {
    private val file = StubData.getFile("SDCardMeasurementsFromSession.csv")
    private val fileLines = file.readLines()
    private val fileLinesCount = fileLines.count()
    private val fileFirstMeasurementTime = CSVSession.timestampFrom(fileLines.first())
    private val fileLastMeasurementTime = CSVSession.timestampFrom(fileLines.last())
    private val helper = MeasurementsAveragingHelperDefault()

    private val streamsCount = 5
    private val sessionId = 1L

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
            val averagingThreshold = helper.calculateAveragingWindow(fileFirstMeasurementTime!!.time, fileLastMeasurementTime!!.time).value
            val iterator = SDCardSessionFileHandlerMobile(mock(), mock(), helper, mock())
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
                on { id } doReturn sessionId
            }
            whenever(sessionsRepository.getSessionByUUID(any())).thenReturn(dbSession)
            val averagingThreshold = helper.calculateAveragingWindow(dbSession.startTime.time, fileLastMeasurementTime!!.time).value
            val iterator = SDCardSessionFileHandlerMobile(mock(),sessionsRepository, helper, mock())
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
                on { id } doReturn sessionId
            }
            whenever(sessionsRepository.getSessionByUUID(any())).thenReturn(dbSession)
            val averagingThreshold = helper.calculateAveragingWindow(dbSession.startTime.time, fileLastMeasurementTime!!.time).value
            val iterator = SDCardSessionFileHandlerMobile(mock(),sessionsRepository, helper, mock())
            val measurementsAveragedCountInFile =
                fileMeasurementsCountAfterAveraging(averagingThreshold)

            val csvSession = iterator.handle(file)

            assertEquals(measurementsAveragedCountInFile, csvMeasurements(csvSession).count())
        }

    @Test
    fun mobile_read_whenFrequencyIs5_buildsAveragedMeasurementCorrectly() =
        runTest {
            val file = StubData.getFile("10SDCardMeasurementsFromSessionToAverage.csv")
            val fileStartTime = DateConverter.fromString(
                "01/06/2023 11:19:37",
                dateFormat = CSVSession.DATE_FORMAT
            )
            val sessionStartTime = calendar().addHours(fileStartTime!!, -3)
            val sessionsRepository = mock<SessionsRepository>()
            val dbSession = mock<SessionDBObject> {
                on { startTime } doReturn sessionStartTime
                on { id } doReturn sessionId
            }
            whenever(sessionsRepository.getSessionByUUID(any())).thenReturn(dbSession)
            val firstExpectedAveragedMeasurementTime =
                calendar().addSeconds(fileStartTime, AveragingWindow.FIRST.value - 1)
            val firstExpectedAveragedFahrenheit = 72.0
            val firstExpectedAveragedRH = 68.0
            val firstExpectedAveragedLatitude = 50.0582475
            val firstExpectedAveragedLongitude = 19.9261414
            val secondExpectedAveragedFahrenheit = 73.0
            val secondExpectedAveragedRH = 44.0
            val secondExpectedAveragedLatitude = 38.0582475
            val secondExpectedAveragedLongitude = 18.9261414
            val secondExpectedAveragedMeasurementTime =
                calendar().addSeconds(firstExpectedAveragedMeasurementTime, AveragingWindow.FIRST.value)
            val iterator = SDCardSessionFileHandlerMobile(mock(),sessionsRepository, helper, mock())

            val csvSession = iterator.handle(file)
            val firstResultFahrenheitAverage =
                csvSession!!.streams[CSVSession.AB3LineParameter.F.position]!![0]
            val secondResultFahrenheitAverage =
                csvSession.streams[CSVSession.AB3LineParameter.F.position]!![1]
            val firstResultRHAverage =
                csvSession.streams[CSVSession.AB3LineParameter.RH.position]!![0]
            val secondResultRHAverage =
                csvSession.streams[CSVSession.AB3LineParameter.RH.position]!![1]

            assertEquals(firstExpectedAveragedFahrenheit, firstResultFahrenheitAverage.value)
            assertEquals(secondExpectedAveragedFahrenheit, secondResultFahrenheitAverage.value)
            assertEquals(firstExpectedAveragedLatitude, firstResultFahrenheitAverage.latitude)
            assertEquals(secondExpectedAveragedLatitude, secondResultFahrenheitAverage.latitude)
            assertEquals(firstExpectedAveragedLongitude, firstResultFahrenheitAverage.longitude)
            assertEquals(secondExpectedAveragedLongitude, secondResultFahrenheitAverage.longitude)
            assertEquals(firstExpectedAveragedMeasurementTime, firstResultFahrenheitAverage.time)
            assertEquals(secondExpectedAveragedMeasurementTime, secondResultFahrenheitAverage.time)
            assertEquals(firstExpectedAveragedRH, firstResultRHAverage.value)
            assertEquals(secondExpectedAveragedRH, secondResultRHAverage.value)
            assertEquals(firstExpectedAveragedLatitude, firstResultRHAverage.latitude)
            assertEquals(secondExpectedAveragedLatitude, secondResultRHAverage.latitude)
            assertEquals(firstExpectedAveragedLongitude, firstResultRHAverage.longitude)
            assertEquals(secondExpectedAveragedLongitude, secondResultRHAverage.longitude)
            assertEquals(firstExpectedAveragedMeasurementTime, firstResultRHAverage.time)
            assertEquals(secondExpectedAveragedMeasurementTime, secondResultRHAverage.time)
        }

    @Test
    fun mobile_read_whenFrequencyIs60_buildsAveragedMeasurementCorrectly() =
        runTest {
            val file = StubData.getFile("60SDCardMeasurementsFromSessionToAverage.csv")
            val fileStartTime = DateConverter.fromString(
                "01/06/2023 11:19:37",
                dateFormat = CSVSession.DATE_FORMAT
            )
            val sessionStartTime = calendar().addHours(fileStartTime!!, -10)
            val sessionsRepository = mock<SessionsRepository>()
            val dbSession = mock<SessionDBObject> {
                on { startTime } doReturn sessionStartTime
                on { id } doReturn sessionId
            }
            whenever(sessionsRepository.getSessionByUUID(any())).thenReturn(dbSession)
            val expectedAveragedMeasurementTime =
                calendar().addSeconds(fileStartTime, AveragingWindow.SECOND.value - 1)
            val expectedAveragedFahrenheit = 73.0
            val expectedAveragedRH = 49.0
            val expectedAveragedLatitude = 78.0582475
            val expectedAveragedLongitude = 90.9261414
            val iterator = SDCardSessionFileHandlerMobile(mock(),sessionsRepository, helper, mock())

            val csvSession = iterator.handle(file)
            val firstResultFahrenheitAverage =
                csvSession!!.streams[CSVSession.AB3LineParameter.F.position]!![0]
            val firstResultRHAverage =
                csvSession.streams[CSVSession.AB3LineParameter.RH.position]!![0]

            assertEquals(expectedAveragedFahrenheit, firstResultFahrenheitAverage.value)
            assertEquals(expectedAveragedLatitude, firstResultFahrenheitAverage.latitude)
            assertEquals(expectedAveragedLongitude, firstResultFahrenheitAverage.longitude)
            assertEquals(expectedAveragedMeasurementTime, firstResultFahrenheitAverage.time)
            assertEquals(expectedAveragedRH, firstResultRHAverage.value)
            assertEquals(expectedAveragedLatitude, firstResultRHAverage.latitude)
            assertEquals(expectedAveragedLongitude, firstResultRHAverage.longitude)
            assertEquals(expectedAveragedMeasurementTime, firstResultRHAverage.time)
        }

    @Test
    fun mobile_read_whenFrequencyIs5_keeps5SecondsMeasurementsDifference() =
        runTest {
            val fiveSeconds = 5 * 1000L
            val file = StubData.getFile("5HoursOfMeasurementsSDCard.csv")
            val sessionsRepository = mock<SessionsRepository>()
            val dbSession = mock<SessionDBObject> {
                on { id } doReturn sessionId
            }
            whenever(sessionsRepository.getSessionByUUID(any())).thenReturn(dbSession)
            val iterator = SDCardSessionFileHandlerMobile(mock(),sessionsRepository, helper, mock())

            val csvSession = iterator.handle(file)

            csvSession?.streams?.values?.forEach { measurements ->
                for (i in 1 until measurements.size) {
                    val difference = measurements[i].time.time - measurements[i - 1].time.time
                    assertEquals(fiveSeconds, difference)
                }
            }
        }

    @Test
    fun mobile_read_whenFrequencyIs60_keeps1MinuteMeasurementsDifference() =
        runTest {
            val oneMinute = 60 * 1000L
            val file = StubData.getFile("14HoursOfMeasurementsSDCard.csv")
            val sessionsRepository = mock<SessionsRepository>()
            val sessionStartTime = DateConverter.fromString(
                "02/08/2023 12:11:12",
                dateFormat = CSVSession.DATE_FORMAT
            )
            val dbSession = mock<SessionDBObject> {
                on { startTime } doReturn sessionStartTime!!
                on { id } doReturn sessionId
            }
            whenever(sessionsRepository.getSessionByUUID(any())).thenReturn(dbSession)
            val iterator = SDCardSessionFileHandlerMobile(mock(),sessionsRepository, helper, mock())

            val csvSession = iterator.handle(file)

            csvSession?.streams?.values?.forEach { measurements ->
                for (i in 1 until measurements.size) {
                    val difference = measurements[i].time.time - measurements[i - 1].time.time
                    assertEquals(
                        oneMinute,
                        difference,
                        "Failing time pair: ${measurements[i].time}, ${measurements[i - 1].time}"
                    )
                }
            }
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