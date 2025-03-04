package pl.llp.aircasting

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pl.llp.aircasting.helpers.MockWebServerDispatcher
import pl.llp.aircasting.helpers.JsonHelper
import pl.llp.aircasting.helpers.awaitForAssertion
import pl.llp.aircasting.ui.view.screens.create_account.CreateAccountActivity
import pl.llp.aircasting.util.Settings
import java.net.HttpURLConnection
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class CreateAccountTest : BaseTest() {
    @Inject
    lateinit var settings: Settings

    @Inject
    override lateinit var server: MockWebServer

    @get:Rule
    val testRule: ActivityTestRule<CreateAccountActivity> =
        ActivityTestRule(CreateAccountActivity::class.java, true, false)

    override fun setup() {
        appComponent.inject(this)
        super.setup()
    }
    @Test
    fun testCreateAccount() {
        settings.onboardingAccepted()

        testRule.launchActivity(null)

        createAccountLogic()

        onView(withId(R.id.email_input)).perform(ViewActions.typeText("maria@example.org"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.profile_name_input)).perform(ViewActions.typeText("maria"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.password_input)).perform(ViewActions.typeText("secret"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.create_account_button)).perform(ViewActions.scrollTo(), click())

        awaitForAssertion {
            onView(withId(R.id.dashboard)).check(matches(isDisplayed()))
            assertEquals(settings.getAuthToken(), "XYZ123FAKETOKEN")
        }

    }

    private fun createAccountLogic() {
        val createAccountResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(
                JsonHelper.buildJson(
                    mapOf(
                        "email" to "maria@example.org",
                        "username" to "maria",
                        "authentication_token" to "XYZ123FAKETOKEN"
                    )
                )
            )

        val syncResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(JsonHelper.buildJson(emptyMap<String, String>()))

        MockWebServerDispatcher.set(
            mapOf(
                "/api/user.json" to createAccountResponse,
                "/api/user/sessions/sync_with_versioning.json" to syncResponse
            ),
            server
        )
    }

    @Test
    fun testCreateAccountErrors() {
        settings.onboardingAccepted()

        val createAccountErrorResponse = MockResponse()
            .setResponseCode(422)
            .setBody(
                JsonHelper.buildJson(
                    mapOf(
                        "email" to listOf("can't be blank")
                    )
                )
            )

        val syncResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(JsonHelper.buildJson(emptyMap<String, String>()))

        MockWebServerDispatcher.set(
            mapOf(
                "/api/user.json" to createAccountErrorResponse,
                "/api/user/sessions/sync_with_versioning.json" to syncResponse
            ),
            server
        )

        testRule.launchActivity(null)

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
