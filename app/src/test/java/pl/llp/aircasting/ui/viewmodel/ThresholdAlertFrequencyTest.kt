package pl.llp.aircasting.ui.viewmodel

import org.junit.Test
import pl.llp.aircasting.R
import kotlin.test.assertEquals

class ThresholdAlertFrequencyTest {
    @Test
    fun getValue() {
        assertEquals("1", ThresholdAlertFrequency.HOURLY.value)
        assertEquals("24", ThresholdAlertFrequency.DAILY.value)
    }

    @Test
    fun getButtonId() {
        assertEquals(R.id.hourly_frequency_button, ThresholdAlertFrequency.HOURLY.buttonId)
        assertEquals(R.id.daily_frequency_button, ThresholdAlertFrequency.DAILY.buttonId)
    }

    @Test
    fun build() {
        assertEquals(ThresholdAlertFrequency.HOURLY, ThresholdAlertFrequency.build(1))
        assertEquals(ThresholdAlertFrequency.DAILY, ThresholdAlertFrequency.build(24))
    }

    @Test
    fun buildFromButtonId() {
        assertEquals(
            ThresholdAlertFrequency.HOURLY,
            ThresholdAlertFrequency.buildFromButtonId(R.id.hourly_frequency_button)
        )
        assertEquals(
            ThresholdAlertFrequency.DAILY,
            ThresholdAlertFrequency.build(R.id.daily_frequency_button)
        )
    }
}