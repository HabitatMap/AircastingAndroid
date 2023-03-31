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
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pl.llp.aircasting.di.TestAppComponent
import pl.llp.aircasting.helpers.*
import pl.llp.aircasting.ui.view.screens.login.LoginActivity
import pl.llp.aircasting.util.Settings
import java.net.HttpURLConnection
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class LoginTest : BaseTest() {
    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var mockServer: MockWebServer

    @get:Rule
    val testRule: ActivityTestRule<LoginActivity> =
        ActivityTestRule(LoginActivity::class.java, false, false)

    override fun setup() {
        (ApplicationProvider.getApplicationContext() as TestApplication)
            .apply {
                (appComponent as TestAppComponent).inject(this@LoginTest)
            }
        server = mockServer
        super.setup()
    }

    @Test
    fun testLogin() {
        val loginResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(
                Util.buildJson(
                    mapOf(
                        "email" to "ania@example.org",
                        "username" to "ania",
                        "authentication_token" to "XYZ123FAKETOKEN"
                    )
                )
            )

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

        awaitForAssertion {
            onView(withId(R.id.dashboard)).check(matches(isDisplayed()))
        }

        assertEquals(settings.getAuthToken(), "XYZ123FAKETOKEN")
    }
}