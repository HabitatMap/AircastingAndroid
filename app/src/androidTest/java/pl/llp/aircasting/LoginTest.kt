package pl.llp.aircasting

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.di.TestApiModule
import pl.llp.aircasting.di.TestPermissionsModule
import pl.llp.aircasting.di.TestSettingsModule
import pl.llp.aircasting.di.modules.AppModule
import pl.llp.aircasting.di.modules.PermissionsModule
import pl.llp.aircasting.helpers.*
import pl.llp.aircasting.ui.view.screens.main.MainActivity
import pl.llp.aircasting.util.Settings
import java.net.HttpURLConnection
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class LoginTest {
    @Inject
    lateinit var settings: Settings

    @get:Rule
    val testRule: ActivityTestRule<MainActivity>
            = ActivityTestRule(MainActivity::class.java, false, false)
    @Inject
    lateinit var server: MockWebServer

    private fun setupDagger() {
        val app = ApplicationProvider.getApplicationContext<AircastingApplication>()
        val permissionsModule = TestPermissionsModule()
        val testAppComponent = DaggerTestAppComponent.builder()
            .appModule(AppModule(app))
            .apiModule(TestApiModule())
            .settingsModule(TestSettingsModule())
            .permissionsModule(permissionsModule)
            .build()
        app.userDependentComponent = testAppComponent
        testAppComponent.inject(this)
    }

    @Before
    fun setup() {
        setupDagger()
        server.start()
    }

    @After
    fun cleanup() {
        server.shutdown()
    }

    @Test
    fun testLogin() {
        val loginResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(Util.buildJson(mapOf(
                "email" to "ania@example.org",
                "username" to "ania",
                "authentication_token" to "XYZ123FAKETOKEN"
            )))

        val syncResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(Util.buildJson(emptyMap<String, String>()))

        MockWebServerDispatcher.set(
            mapOf(
                "/api/user.json" to loginResponse,
                "/api/user/sessions/sync_with_versioning.json" to syncResponse
            ),
            server
        )
        settings.onboardingAccepted()

        testRule.launchActivity(null)

        onView(withId(R.id.profile_name_input)).perform(ViewActions.typeText("ania@example.org"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.password_input)).perform(ViewActions.typeText("secret"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.login_button)).perform(click())

        waitAndRetry {
            onView(withId(R.id.dashboard)).check(matches(isDisplayed()))
        }

        assertEquals(settings.getAuthToken(), "XYZ123FAKETOKEN")
    }
}