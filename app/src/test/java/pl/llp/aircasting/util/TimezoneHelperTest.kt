package pl.llp.aircasting.util

import org.junit.Test
import kotlin.test.assertEquals

class TimezoneHelperTest {
    @Test
    fun getTimezoneOffsetInMillis() {
        val offset = TimezoneHelper.getTimezoneOffsetInSeconds()

        assertEquals(3600, offset)
    }
}