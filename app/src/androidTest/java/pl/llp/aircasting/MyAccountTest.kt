package pl.llp.aircasting

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import junit.framework.Assert.assertTrue
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import pl.llp.aircasting.data.api.util.ApiConstants.urlSync
import pl.llp.aircasting.data.local.AppDatabase
import pl.llp.aircasting.helpers.MockWebServerDispatcher
import pl.llp.aircasting.helpers.awaitForAssertion
import pl.llp.aircasting.ui.view.screens.login.LoginActivity
import pl.llp.aircasting.ui.view.screens.settings.my_account.MyAccountActivity
import pl.llp.aircasting.util.Settings
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class MyAccountTest : BaseTest() {

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var db: AppDatabase

    @Inject
    lateinit var mockServer: MockWebServer

    @get:Rule
    val testRule: ActivityTestRule<MyAccountActivity> =
        ActivityTestRule(MyAccountActivity::class.java, false, false)

    @Before
    override fun setup() {
        userComponent?.inject(this)
        server = mockServer
        super.setup()
    }

    @After
    override fun cleanup() {
        testRule.finishActivity()
        super.cleanup()
    }

    @Ignore("MockWebServer returns error on sync response stub for some reason")
    @Test
    fun myAccountTest() {
        Intents.init()
        settings.login("michal@lunarlogic.io", "EMAIL", "XYZ123FAKETOKEN")

        testRule.launchActivity(null)

        onView(withId(R.id.header)).check(matches(withText("You are currently logged in as\n${settings.getEmail()}")))

        onView(withId(R.id.sign_out_button)).perform(click())

        val syncResponseMock = MockResponse()
            .setResponseCode(200)
        MockWebServerDispatcher.set(
            mapOf(
                urlSync to syncResponseMock
            ),
            server
        )

        awaitForAssertion {
            assertEquals(null, settings.getAuthToken())
            assertEquals(null, settings.getEmail())

            val measurements = db.measurements().getAll()
            val streams = db.measurementStreams().getAll()
            val sessions = db.sessions().getAll()
            val sensorThresholds = db.sensorThresholds().getAll()
            assert(measurements.isEmpty())
            assert(streams.isEmpty())
            assert(sessions.isEmpty())
            assert(sensorThresholds.isEmpty())
        }

        // checking if LoginActivity is launched:
        intended(hasComponent(LoginActivity::class.java.name))

        Espresso.pressBackUnconditionally()
        assertTrue(testRule.activity.isDestroyed)
    }
}
