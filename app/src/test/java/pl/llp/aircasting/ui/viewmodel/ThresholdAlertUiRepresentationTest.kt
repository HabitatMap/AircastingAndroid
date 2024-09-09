package pl.llp.aircasting.ui.viewmodel

import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import pl.llp.aircasting.data.api.util.StringConstants.airbeamsensorName
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.TemperatureConverter
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

class ThresholdAlertUiRepresentationTest {

    @Before
    fun setup() {
        val settings = mock<Settings> {
            on { isCelsiusScaleEnabled() } doReturn true
        }
        TemperatureConverter.setup(settings)
    }

    @Test
    fun getStreamTitle_returnsDetailedTypeOfStream() {
        val stream = mock<MeasurementStream> {
            on { detailedType } doReturn "F"
            on { sensorName } doReturn airbeamsensorName
        }
        val uiAlert = ThresholdAlertUiRepresentation(stream)

        val result = uiAlert.streamTitle

        verify(stream).detailedType
        assertEquals(stream.detailedType, result)
    }

    @Test
    fun getThresholdUi_whenStreamDetailedTypeIsCelsius_returnsValueConvertedToCelsius() {
        val stream = mock<MeasurementStream> {
            on { isDetailedTypeCelsius() } doReturn true
            on { sensorName } doReturn airbeamsensorName
        }
        val threshold = 32.0
        val uiAlert = ThresholdAlertUiRepresentation(stream, _threshold = threshold)

        val result = uiAlert.getThresholdUi()

        assertEquals(TemperatureConverter.fahrenheitToCelsius(threshold), result)
    }

    @Test
    fun getThresholdUi_whenStreamDetailedTypeIsFahrenheit_returnsThresholdValue() {
        val stream = mock<MeasurementStream> {
            on { isDetailedTypeCelsius() } doReturn false
            on { sensorName } doReturn airbeamsensorName
        }
        val threshold = 32.0
        val uiAlert = ThresholdAlertUiRepresentation(stream, _threshold = threshold)

        val result = uiAlert.getThresholdUi()

        assertEquals(threshold, result)
    }

    @Test
    fun setThresholdUi_whenStreamDetailedTypeIsCelsius_convertsValueToCelsius() {
        val stream = mock<MeasurementStream> {
            on { isDetailedTypeCelsius() } doReturn true
            on { sensorName } doReturn airbeamsensorName
        }
        val uiThreshold = 32.0
        val uiAlert = ThresholdAlertUiRepresentation(stream)

        uiAlert.setThresholdUi(uiThreshold)

        assertEquals(TemperatureConverter.celsiusToFahrenheit(uiThreshold), uiAlert.threshold)
    }

    @Test
    fun setThresholdUi_whenStreamDetailedTypeIsNotCelsius_setsUiThresholdValueDirectly() {
        val stream = mock<MeasurementStream> {
            on { isDetailedTypeCelsius() } doReturn false
            on { sensorName } doReturn airbeamsensorName
        }
        val uiThreshold = 32.0
        val uiAlert = ThresholdAlertUiRepresentation(stream)

        uiAlert.setThresholdUi(uiThreshold)

        assertEquals(uiThreshold, uiAlert.threshold)
    }

    @Test
    fun copy() {
        val uiAlert = ThresholdAlertUiRepresentation(mock(), airbeamsensorName)
        val copy = uiAlert.copy()

        assertNotSame(uiAlert, copy)
        assertEquals(uiAlert, copy)
    }
}