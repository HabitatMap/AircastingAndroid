package pl.llp.aircasting.ui.view.screens.settings

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import pl.llp.aircasting.data.api.params.UserSettingsBody
import pl.llp.aircasting.data.api.params.UserSettingsData
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.util.Settings

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class SettingsControllerTest {

    @Mock
    lateinit var apiService: ApiService

    @Mock
    lateinit var settings: Settings

    lateinit var controller: SettingsController

    private val testCoroutineScope = TestScope()

    @Before
    fun setup() {
        controller = SettingsController(
            mock(),
            mock(),
            mock(),
            fragmentManager = mock(),
            coroutineScope = testCoroutineScope,
            mSettings = settings,
            loginService = mock(),
            logoutService = mock(),
            apiService = apiService
        )
    }

    @Test
    fun onToggleDormantStreamAlert() = testCoroutineScope.runTest {
        val enabled = false

        controller.onToggleDormantStreamAlert(enabled)
        yield()

        verify(apiService).updateUserSettings(eq(UserSettingsBody(UserSettingsData(enabled))))
        verify(settings).toggleDormantStreamAlert(eq(enabled))
    }
}