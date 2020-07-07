package io.lunarlogic.aircasting

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import okhttp3.mockwebserver.MockResponse
import io.lunarlogic.aircasting.di.AppModule
import io.lunarlogic.aircasting.di.MockWebServerModule
import io.lunarlogic.aircasting.di.PermissionsModule
import io.lunarlogic.aircasting.di.TestSettingsModule
import io.lunarlogic.aircasting.helpers.JsonBody
import io.lunarlogic.aircasting.helpers.MockWebServerDispatcher
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.main.MainActivity
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import org.junit.Assert.assertEquals

import org.junit.runner.RunWith

import java.net.HttpURLConnection
import javax.inject.Inject


@RunWith(AndroidJUnit4::class)
class LoginTest {
    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var mockWebServer: MockWebServer

    @get:Rule
    val testRule: ActivityTestRule<MainActivity>
            = ActivityTestRule(MainActivity::class.java, false, false)

    private fun setupDagger() {
        val app = ApplicationProvider.getApplicationContext<AircastingApplication>()
        val permissionsModule = PermissionsModule()
        val testAppComponent = DaggerTestAppComponent.builder()
            .appModule(AppModule(app))
            .settingsModule(TestSettingsModule())
            .permissionsModule(permissionsModule)
            .mockWebServerModule(MockWebServerModule())
            .build()
        app.appComponent = testAppComponent
        testAppComponent.inject(this)
    }

    @Before
    fun setup() {
        setupDagger()
    }

    @After
    fun cleanup() {
        mockWebServer.shutdown()
    }

    @Test
    fun testLogin() {
        val loginResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(JsonBody.build(mapOf(
                "email" to "ania@example.org",
                "username" to "ania",
                "authentication_token" to "XYZ123FAKETOKEN"
            )))

        val syncResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(JsonBody.build(emptyMap<String, String>()))

        MockWebServerDispatcher.set(
            mapOf(
                "/api/user.json" to loginResponse,
                "/api/user/sessions/sync_with_versioning.json" to syncResponse
            ),
            mockWebServer
        )

        testRule.launchActivity(null)

        onView(withId(R.id.username)).perform(ViewActions.typeText("ania@example.org"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.password)).perform(ViewActions.typeText("secret"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.login_button)).perform(click())

        Thread.sleep(2000)

        onView(withId(R.id.dashboard)).check(matches(isDisplayed()))
        assertEquals(settings.getAuthToken(), "XYZ123FAKETOKEN")
    }
}