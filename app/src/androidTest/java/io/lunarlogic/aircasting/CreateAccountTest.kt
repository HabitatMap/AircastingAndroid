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
import io.lunarlogic.aircasting.di.*
import okhttp3.mockwebserver.MockResponse
import io.lunarlogic.aircasting.helpers.JsonBody
import io.lunarlogic.aircasting.helpers.MockWebServerDispatcher
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.screens.main.MainActivity
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers
import org.junit.*
import org.junit.Assert.assertEquals

import org.junit.runner.RunWith

import java.net.HttpURLConnection
import javax.inject.Inject


@RunWith(AndroidJUnit4::class)
class CreateAccountTest {
    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var apiFactory: ApiServiceFactory

    @get:Rule
    val testRule: ActivityTestRule<MainActivity>
            = ActivityTestRule(MainActivity::class.java, false, false)

    private fun setupDagger() {
        val app = ApplicationProvider.getApplicationContext<AircastingApplication>()
        val permissionsModule = PermissionsModule()
        val testAppComponent = DaggerTestAppComponent.builder()
            .appModule(AppModule(app))
            .apiModule(FakeApiServiceFactoryModule())
            .settingsModule(TestSettingsModule())
            .permissionsModule(permissionsModule)
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
//        mockWebServer.shutdown()
    }

    @Test
    fun testCreateAccount() {
        val createAccountResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(
                JsonBody.build(
                    mapOf(
                        "email" to "maria@example.org",
                        "username" to "maria",
                        "authentication_token" to "XYZ123FAKETOKEN"
                    )
                )
            )

        val syncResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(JsonBody.build(emptyMap<String, String>()))

        MockWebServerDispatcher.set(
            mapOf(
                "/api/user.json" to createAccountResponse,
                "/api/user/sessions/sync_with_versioning.json" to syncResponse
            ),
            (apiFactory as FakeApiServiceFactory).mockWebServer
        )

        testRule.launchActivity(null)

        onView(withId(R.id.create_account_button)).perform(ViewActions.scrollTo(), click())
        Thread.sleep(500)
        onView(withId(R.id.email_input)).perform(ViewActions.typeText("maria@example.org"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.username_input)).perform(ViewActions.typeText("maria"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.password_input)).perform(ViewActions.typeText("secret"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.create_account_button)).perform(ViewActions.scrollTo(), click())

        Thread.sleep(2000)

        onView(withId(R.id.dashboard)).check(matches(isDisplayed()))
        assertEquals(settings.getAuthToken(), "XYZ123FAKETOKEN")
    }

    @Test
    fun testCreateAccountErrors() {
        val createAccountErrorResponse = MockResponse()
            .setResponseCode(422)
            .setBody(
                JsonBody.build(
                    mapOf(
                        "email" to listOf<String>("can't be blank")
                    )
                )
            )

        val syncResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(JsonBody.build(emptyMap<String, String>()))

        MockWebServerDispatcher.set(
            mapOf(
                "/api/user.json" to createAccountErrorResponse,
                "/api/user/sessions/sync_with_versioning.json" to syncResponse
            ),
            (apiFactory as FakeApiServiceFactory).mockWebServer
        )

        testRule.launchActivity(null)

        Espresso.closeSoftKeyboard()
        onView(withId(R.id.create_account_button)).perform(ViewActions.scrollTo(), click())
        Thread.sleep(500)
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.username_input)).perform(ViewActions.typeText("maria"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.password_input)).perform(ViewActions.typeText("secret"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.create_account_button)).perform(ViewActions.scrollTo(), click())

        Thread.sleep(2000)

        onView(withText("can't be blank")).check(matches(isDisplayed()))
    }
}
