package pl.llp.aircasting.ui.viewmodel

import com.ibm.icu.impl.Assert.fail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.internal.util.reflection.ReflectionMemberAccessor
import org.mockito.kotlin.*
import pl.llp.aircasting.data.api.params.CreateThresholdAlertData
import pl.llp.aircasting.data.api.params.ThresholdAlertResponse
import pl.llp.aircasting.data.api.repository.ThresholdAlertRepositoryDefault
import pl.llp.aircasting.data.api.util.StringConstants.O3
import pl.llp.aircasting.data.api.util.StringConstants.PM2_5
import pl.llp.aircasting.data.api.util.StringConstants.responseAirbeam2SensorName
import pl.llp.aircasting.data.api.util.StringConstants.responseOpenAQSensorNameOzone
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.TimezoneHelper
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CreateThresholdAlertBottomSheetViewModelTest {

    private lateinit var alertRepository: ThresholdAlertRepositoryDefault

    private val testScope = TestScope()
    private val uuid = "uuid"
    private val stream = mock<MeasurementStream> {
        on { sensorName } doReturn responseAirbeam2SensorName
        on { detailedType } doReturn PM2_5
    }

    @Captor
    lateinit var createDataCaptor: ArgumentCaptor<CreateThresholdAlertData>
    private lateinit var deleteIdCaptor: ArgumentCaptor<Int>

    @Test
    fun getAlertsForDisplaying_whenSessionHasAlert_constructsUiAlertsWithInfoFromBackend() =
        testScope.runTest {
            val session = mockSession(listOf(stream))
            val alerts = mockAlerts()
            val frequencyValue = alerts.first().frequency
            val thresholdValue = alerts.first().thresholdValue
            alertRepository = mock {
                on { activeAlerts() } doReturn flow { emit(alerts) }
            }
            val viewModel = CreateThresholdAlertBottomSheetViewModel(alertRepository)

            viewModel.getAlertsForDisplaying(session).collect { result ->
                result.onSuccess { uiAlerts ->
                    val uiAlert =
                        uiAlerts.find { it.sensorName == responseAirbeam2SensorName }!!
                    assertTrue(uiAlert.enabled)
                    assertEquals(stream, uiAlert.stream)
                    assertEquals(thresholdValue, uiAlert.threshold)
                    assertEquals(frequencyValue.toString(), uiAlert.frequency.value)
                }
                    .onFailure { fail("Result was a failure") }
            }
        }

    @Test
    fun getAlertsForDisplaying_whenSessionDoesNotHaveAlert_constructsDefaultUiAlertsForCorrespondingStreams() =
        testScope.runTest {
            val stream2 = mock<MeasurementStream> {
                on { sensorName } doReturn responseOpenAQSensorNameOzone
                on { detailedType } doReturn O3
            }
            val defaultUiAlert1 = ThresholdAlertUiRepresentation(stream)
            val defaultUiAlert2 = ThresholdAlertUiRepresentation(stream2)
            val streams = listOf(stream, stream2)
            val session = mockSession(streams)
            alertRepository = mock {
                on { activeAlerts() } doReturn flow { emit(emptyList()) }
            }
            val viewModel = CreateThresholdAlertBottomSheetViewModel(alertRepository)

            viewModel.getAlertsForDisplaying(session).collect { result ->
                result.onSuccess { uiAlerts ->
                    val uiAlert1 =
                        uiAlerts.find { it.stream == stream }!!
                    val uiAlert2 =
                        uiAlerts.find { it.stream == stream2 }!!

                    assertEquals(streams.size, uiAlerts.size)
                    assertFalse(uiAlert1.enabled)
                    assertFalse(uiAlert2.enabled)
                    assertEquals(defaultUiAlert1, uiAlert1)
                    assertEquals(defaultUiAlert2, uiAlert2)
                }
                    .onFailure { fail("Result was a failure") }
            }
        }

    @Test
    fun getAlertsForDisplaying_whenModifyingReturnedValue_cachedValueDoesNotChange() =
        testScope.runTest {
            val session = mockSession(listOf(stream))
            alertRepository = mock {
                on { activeAlerts() } doReturn flow { emit(emptyList()) }
            }
            val viewModel = CreateThresholdAlertBottomSheetViewModel(alertRepository)

            viewModel.getAlertsForDisplaying(session).collect { result ->
                result.onSuccess { uiAlertsCopy ->
                    val uiAlertsField = viewModel.javaClass.getDeclaredField("uiAlerts")
                    val viewModelUiAlerts =
                        ReflectionMemberAccessor().get(uiAlertsField, viewModel) as List<*>
                    val viewModelUiAlert =
                        viewModelUiAlerts.first() as ThresholdAlertUiRepresentation
                    val resultUiAlert = uiAlertsCopy.first()

                    resultUiAlert.enabled = !resultUiAlert.enabled

                    assertNotEquals(resultUiAlert.enabled, viewModelUiAlert.enabled)
                }
                    .onFailure { fail("Result was a failure") }
            }
        }

    @Test
    fun getAlertsForDisplaying_whenSessionIsNull_emitsFailure() = testScope.runTest {
        alertRepository = mock()
        val viewModel = CreateThresholdAlertBottomSheetViewModel(alertRepository)

        viewModel.getAlertsForDisplaying(null).collect { result ->
            result.onSuccess {
                fail("Result was successful when session was null")
            }
        }
    }

    @Test
    fun saveEditedAlerts_whenCannotFindModifiedUiAlert_amongstOldOnes_returnsFailingResult() =
        testScope.runTest {
            val uiAlerts = emptyList<ThresholdAlertUiRepresentation>()
            val modifiedUiAlert = ThresholdAlertUiRepresentation(stream)
            alertRepository = mock()
            val viewModel = CreateThresholdAlertBottomSheetViewModel(alertRepository)
            setAlertsFields(viewModel, uiAlerts = uiAlerts)

            viewModel.saveEditedAlerts(listOf(modifiedUiAlert), null).collect { result ->
                result.onSuccess {
                    fail("Result was successful when cached and modified uiAlerts did not correspond to each other")
                }
            }
        }

    @Test
    fun saveEditedAlerts_whenAlertWasDeleted_deletesAlert() = testScope.runTest {
        val uiAlert = ThresholdAlertUiRepresentation(stream, _enabled = true)
        val uiAlerts = listOf(uiAlert)
        val uiAlertsCopy = uiAlerts.map { it.copy() }
        val session = mockSession(listOf(stream))
        val alerts = mockAlerts()
        val id = alerts.first().id
        alertRepository = mock()
        val viewModel = CreateThresholdAlertBottomSheetViewModel(alertRepository)
        setAlertsFields(viewModel, alerts, uiAlerts)

        uiAlertsCopy.first().enabled = false
        viewModel.saveEditedAlerts(uiAlertsCopy, session).collect { result ->
            result.onSuccess {
                verify(alertRepository).delete(id)
                verify(alertRepository, times(0)).create(any())
            }
                .onFailure { fail("Result was a failure") }
        }
    }

    @Test
    fun saveEditedAlerts_whenAlertWasCreated_createsAlert() = testScope.runTest {
        val uiAlert = ThresholdAlertUiRepresentation(stream)
        val uiAlerts = listOf(uiAlert)
        val uiAlertsCopy = uiAlerts.map { it.copy() }
        val session = mockSession(listOf(stream))
        alertRepository = mock()
        val viewModel = CreateThresholdAlertBottomSheetViewModel(alertRepository)
        setAlertsFields(viewModel, uiAlerts = uiAlerts)

        val uiAlertCopy = uiAlertsCopy.first()
        uiAlertCopy.apply {
            enabled = true
            threshold = 10.0
            frequency = ThresholdAlertFrequency.DAILY
        }
        val uiAlertCopyData = CreateThresholdAlertData(
            uiAlertCopy.frequency.value,
            uiAlertCopy.sensorName,
            session.uuid,
            uiAlertCopy.threshold.toString(),
            TimezoneHelper.getTimezoneOffsetInSeconds().toString()
        )
        viewModel.saveEditedAlerts(uiAlertsCopy, session).collect { result ->
            result.onSuccess {
                verify(alertRepository).create(eq(uiAlertCopyData))
                verify(alertRepository, times(0)).delete(any())
            }
                .onFailure { fail("Result was a failure") }
        }
    }

    @Test
    fun saveEditedAlerts_whenAlertWasModified_replacesAlert() = testScope.runTest {
        val uiAlert = ThresholdAlertUiRepresentation(stream, _enabled = true)
        val uiAlerts = listOf(uiAlert)
        val uiAlertsCopy = uiAlerts.map { it.copy() }
        val session = mockSession(listOf(stream))
        val alerts = mockAlerts()
        val id = alerts.first().id
        alertRepository = mock()
        val viewModel = CreateThresholdAlertBottomSheetViewModel(alertRepository)
        setAlertsFields(viewModel, alerts, uiAlerts)

        val uiAlertCopy = uiAlertsCopy.first()
        uiAlertCopy.apply {
            threshold = 10.0
            frequency = ThresholdAlertFrequency.DAILY
        }
        val uiAlertCopyData = CreateThresholdAlertData(
            uiAlertCopy.frequency.value,
            uiAlertCopy.sensorName,
            session.uuid,
            uiAlertCopy.threshold.toString(),
            TimezoneHelper.getTimezoneOffsetInSeconds().toString()
        )
        viewModel.saveEditedAlerts(uiAlertsCopy, session).collect { result ->
            result.onSuccess {
                inOrder(alertRepository) {
                    verify(alertRepository).delete(id)
                    verify(alertRepository).create(eq(uiAlertCopyData))
                }
            }
                .onFailure { fail("Result was a failure") }
        }
    }

    @Test
    fun saveEditedAlerts_handlesChangesToMultipleAlerts() = testScope.runTest {
        // given
        createDataCaptor = ArgumentCaptor.forClass(CreateThresholdAlertData::class.java)
        deleteIdCaptor = ArgumentCaptor.forClass(Int::class.java)
        val uiAlertToBeDeleted = ThresholdAlertUiRepresentation(stream, _enabled = true)
        val alertToBeDeleted = mockAlert(id = 0)
        val idToBeDeleted = alertToBeDeleted.id

        val streamForCreated = mock<MeasurementStream> {
            on { sensorName } doReturn "AirBeam2-PM10"
            on { detailedType } doReturn "PM10"
        }
        val uiAlertToBeCreated = ThresholdAlertUiRepresentation(streamForCreated, _enabled = false)

        val sensorNameForModified = "AirBeam2-PM1"
        val streamForModified = mock<MeasurementStream> {
            on { sensorName } doReturn sensorNameForModified
            on { detailedType } doReturn "PM1"
        }
        val uiAlertToBeModified = ThresholdAlertUiRepresentation(streamForModified, _enabled = true)
        val idToBeModified = 2
        val alertToBeModified =
            mockAlert(1, idToBeModified, 0.0, sensorName = sensorNameForModified)

        val session = mockSession(listOf(stream, streamForCreated, streamForModified))
        val uiAlerts = listOf(uiAlertToBeDeleted, uiAlertToBeCreated, uiAlertToBeModified)
        val alerts = listOf(alertToBeDeleted, alertToBeModified)
        alertRepository = mock()
        val viewModel = CreateThresholdAlertBottomSheetViewModel(alertRepository)
        setAlertsFields(viewModel, alerts, uiAlerts)

        val uiAlertsCopy = uiAlerts.map { it.copy() }

        // when
        val uiAlertCopyToBeDeleted = uiAlertsCopy.find { it.stream == stream }!!
        uiAlertCopyToBeDeleted.enabled = false

        val uiAlertCopyToBeModified = uiAlertsCopy.find { it.stream == streamForModified }!!
        uiAlertCopyToBeModified.apply {
            threshold = 10.0
            frequency = ThresholdAlertFrequency.DAILY
        }
        val uiAlertCopyToBeModifiedData = CreateThresholdAlertData(
            uiAlertCopyToBeModified.frequency.value,
            uiAlertCopyToBeModified.sensorName,
            session.uuid,
            uiAlertCopyToBeModified.threshold.toString(),
            TimezoneHelper.getTimezoneOffsetInSeconds().toString()
        )

        val uiAlertCopyToBeCreated = uiAlertsCopy.find { it.stream == streamForCreated }!!
        uiAlertCopyToBeCreated.enabled = true
        val uiAlertCopyToBeCreatedData = CreateThresholdAlertData(
            uiAlertCopyToBeCreated.frequency.value,
            uiAlertCopyToBeCreated.sensorName,
            session.uuid,
            uiAlertCopyToBeCreated.threshold.toString(),
            TimezoneHelper.getTimezoneOffsetInSeconds().toString()
        )

        viewModel.saveEditedAlerts(uiAlertsCopy, session).collect { result ->
            result.onSuccess {
                // then
                verify(alertRepository, times(2)).create(capture(createDataCaptor))
                createDataCaptor.allValues.forEach { createData ->
                    assertTrue {
                        createData.equals(uiAlertCopyToBeModifiedData)
                                || createData.equals(uiAlertCopyToBeCreatedData)
                    }
                }

                verify(alertRepository, times(2)).delete(capture(deleteIdCaptor))
                deleteIdCaptor.allValues.forEach { id ->
                    assertTrue {
                        id.equals(idToBeDeleted) || id.equals(idToBeModified)
                    }
                }
            }
                .onFailure { fail("Result was a failure") }
        }
    }

    private fun setAlertsFields(
        viewModel: CreateThresholdAlertBottomSheetViewModel,
        alerts: List<ThresholdAlertResponse>? = null,
        uiAlerts: List<ThresholdAlertUiRepresentation>? = null
    ) {
        val alertsField = viewModel.javaClass.getDeclaredField("alerts")
        val uiAlertsField = viewModel.javaClass.getDeclaredField("uiAlerts")
        ReflectionMemberAccessor().set(alertsField, viewModel, alerts)
        ReflectionMemberAccessor().set(uiAlertsField, viewModel, uiAlerts)
    }

    private fun mockAlerts(): List<ThresholdAlertResponse> {
        val alert = mockAlert()
        return listOf(alert)
    }

    private fun mockAlert(
        frequencyValue: Int = 1,
        id: Int = 1,
        thresholdValue: Double = 40.0,
        sensorName: String = responseAirbeam2SensorName
    ) = ThresholdAlertResponse(
        frequencyValue,
        id,
        sensorName,
        uuid,
        thresholdValue,
        TimezoneHelper.getTimezoneOffsetInSeconds()
    )

    private fun mockSession(streams: List<MeasurementStream>): Session {
        val session = mock<Session> {
            on { it.uuid } doReturn uuid
            on { it.streams } doReturn streams
        }
        return session
    }
}