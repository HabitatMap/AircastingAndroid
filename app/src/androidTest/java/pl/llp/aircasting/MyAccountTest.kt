package pl.llp.aircasting

import androidx.test.core.app.ApplicationProvider
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
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import org.junit.runner.RunWith
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.local.AppDatabase

import pl.llp.aircasting.di.TestApiModule
import pl.llp.aircasting.di.TestPermissionsModule
import pl.llp.aircasting.di.TestSettingsModule
import pl.llp.aircasting.di.modules.AppModule
import pl.llp.aircasting.di.modules.PermissionsModule
import pl.llp.aircasting.helpers.MockWebServerDispatcher
import pl.llp.aircasting.helpers.getFakeApiServiceFactoryFrom
import pl.llp.aircasting.helpers.getMockWebServerFrom
import pl.llp.aircasting.ui.view.screens.login.LoginActivity
import pl.llp.aircasting.ui.view.screens.settings.my_account.MyAccountActivity
import pl.llp.aircasting.util.Settings
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class MyAccountTest {

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var db: AppDatabase

    @Inject
    lateinit var server: MockWebServer

    @get:Rule
    val testRule: ActivityTestRule<MyAccountActivity> =
        ActivityTestRule(MyAccountActivity::class.java, false, false)

    val app = ApplicationProvider.getApplicationContext<AircastingApplication>()

    private fun setupDagger() {
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
        testRule.finishActivity()
        db.close()
        server.shutdown()
    }

    @Ignore("Make assertion wait for full logout process with sync")
    @Test
    fun myAccountTest() {
        Intents.init()
        settings.login("michal@lunarlogic.io", "EMAIL", "XYZ123FAKETOKEN")

        testRule.launchActivity(null)

        // checking if text on text view matches what I want:
        onView(withId(R.id.header)).check(matches(withText("You are currently logged in as\n${settings.getEmail()}")))

        //  performing click on button:
        onView(withId(R.id.sign_out_button)).perform(click())

        val syncResponseMock = MockResponse()
            .setResponseCode(200)
        MockWebServerDispatcher.set(
            mapOf(
                "/api/realtime/sync_measurements.json" to syncResponseMock
            ),
            server
        )

        Thread.sleep(5000)

        // TODO: Make assertion wait for full logout process with sync
        assertEquals(null, settings.getAuthToken())
        assertEquals(null, settings.getEmail())

        // checking if database tables are empty:
        val measurements = db.measurements().getAll()
        val streams = db.measurementStreams().getAll()
        val sessions = db.sessions().getAll()
        val sensorThresholds = db.sensorThresholds().getAll()

        assert(measurements.isEmpty())
        assert(streams.isEmpty())
        assert(sessions.isEmpty())
        assert(sensorThresholds.isEmpty())

        // checking if LoginActivity is launched:
        intended(hasComponent(LoginActivity::class.java.name))

        Espresso.pressBackUnconditionally()
        assertTrue(testRule.activity.isDestroyed)
    }

}
