package pl.llp.aircasting

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import okhttp3.mockwebserver.MockResponse
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pl.llp.aircasting.di.AppModule
import pl.llp.aircasting.di.PermissionsModule
import pl.llp.aircasting.di.TestApiModule
import pl.llp.aircasting.di.TestSettingsModule
import pl.llp.aircasting.helpers.JsonBody
import pl.llp.aircasting.helpers.MockWebServerDispatcher
import pl.llp.aircasting.helpers.getFakeApiServiceFactoryFrom
import pl.llp.aircasting.helpers.getMockWebServerFrom
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.networking.services.ApiServiceFactory
import pl.llp.aircasting.screens.main.MainActivity
import java.net.HttpURLConnection
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class CreateAccountTest {
    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var apiServiceFactory: ApiServiceFactory

    @get:Rule
    val testRule: ActivityScenario<MainActivity> = ActivityScenario.launch(MainActivity::class.java)

    private fun setupDagger() {
        val app = ApplicationProvider.getApplicationContext<AircastingApplication>()
        val permissionsModule = PermissionsModule()
        val testAppComponent = DaggerTestAppComponent.builder()
            .appModule(AppModule(app))
            .apiModule(TestApiModule())
            .settingsModule(TestSettingsModule())
            .permissionsModule(permissionsModule)
            .build()
        app.appComponent = testAppComponent
        testAppComponent.inject(this)
    }

    @Before
    fun setup() {
        setupDagger()
        getMockWebServerFrom(apiServiceFactory).start()
    }

    @After
    fun cleanup() {
        getMockWebServerFrom(apiServiceFactory).shutdown()
    }

    @Test
    fun testCreateAccount() {
        settings.onboardingAccepted()

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
            getFakeApiServiceFactoryFrom(apiServiceFactory).mockWebServer
        )

        testRule.moveToState(Lifecycle.State.STARTED)

        Espresso.closeSoftKeyboard()
        onView(withId(R.id.create_account_button)).perform(ViewActions.scrollTo(), click())
        Thread.sleep(500)
        onView(withId(R.id.email_input)).perform(ViewActions.typeText("maria@example.org"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.profile_name_input)).perform(ViewActions.typeText("maria"))
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
        settings.onboardingAccepted()

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
            getFakeApiServiceFactoryFrom(apiServiceFactory).mockWebServer
        )

        testRule.moveToState(Lifecycle.State.STARTED)

        Espresso.closeSoftKeyboard()
        onView(withId(R.id.create_account_button)).perform(ViewActions.scrollTo(), click())
        Thread.sleep(500)
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.profile_name_input)).perform(ViewActions.typeText("maria"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.password_input)).perform(ViewActions.typeText("secret"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.create_account_button)).perform(ViewActions.scrollTo(), click())

        Thread.sleep(2000)

        onView(withText("can't be blank")).check(matches(isDisplayed()))
    }
}
