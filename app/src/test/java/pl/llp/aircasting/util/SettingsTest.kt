package pl.llp.aircasting.util

import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
class SettingsTest {

    @Mock
    lateinit var sharedPreferences: SharedPreferences

    @Mock
    lateinit var editor: Editor

    lateinit var settings: Settings

    @Before
    fun setup() {
        whenever(sharedPreferences.edit()).doReturn(editor)
        settings = Settings(sharedPreferences)
    }

    @Test
    fun login_savesParametersToSharedPreferences() {
        val profileName = "name"
        val email = "email"
        val authToken = "token"
        val dormantStreamAlert = true

        settings.login(profileName, email, authToken, dormantStreamAlert)

        inOrder(editor) {
            verify(editor).putString(any(), eq(profileName))
            verify(editor).apply()
        }
        verify(editor).putString(any(), eq(email))
        verify(editor).putString(any(), eq(authToken))
        verify(editor).putBoolean(any(), eq(dormantStreamAlert))
    }

    @Test
    fun isDormantStreamAlertEnabled_shouldUseTrueByDefault() {
        settings.isDormantStreamAlertEnabled()
        val argumentCaptor = ArgumentCaptor.forClass(Boolean::class.java)
        verify(sharedPreferences).getBoolean(any(), argumentCaptor.capture())

        assertTrue(argumentCaptor.value)
    }
}