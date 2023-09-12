package pl.llp.aircasting.util

import org.junit.Test
import pl.llp.aircasting.util.extensions.calendar
import kotlin.test.assertEquals

internal class TimezoneHelperTest {
    @Test
    fun getTimezoneOffsetInSeconds() {
        val expectedSecondsOffset = calendar().timeZone.rawOffset / 1000
        val secondsOffset = TimezoneHelper.getTimezoneOffsetInSeconds()

        assertEquals(expectedSecondsOffset, secondsOffset)
    }
}