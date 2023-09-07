package pl.llp.aircasting

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pl.llp.aircasting.data.local.AppDatabase
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.di.*
import pl.llp.aircasting.di.mocks.FakeFixedSessionDetailsController
import pl.llp.aircasting.helpers.*
import pl.llp.aircasting.ui.view.screens.dashboard.DashboardPagerAdapter
import pl.llp.aircasting.ui.view.screens.main.MainActivity
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.Settings
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

@RunWith(AndroidJUnit4::class)
class FixedSessionTest : BaseTest() {
    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var sessionsRepository: SessionsRepository

    @Inject
    lateinit var measurementStreamRepository: MeasurementStreamsRepository

    @Inject
    lateinit var measurementsRepository: MeasurementsRepositoryImpl

    @Inject
    lateinit var database: AppDatabase

    @Inject
    override lateinit var server: MockWebServer

    private val measurementValue = 70.0
    private val stream = MeasurementStream(
        "AirBeam2:0018961070D6",
        "AirBeam2-F",
        "Temperature",
        "F",
        "degrees Fahrenheit",
        "F",
        15,
        45,
        75,
        100,
        135,
        false
    )
    private val measurements = listOf(Measurement(measurementValue, Date()))

    @get:Rule
    val testRule: ActivityTestRule<MainActivity> =
        ActivityTestRule(MainActivity::class.java, true, false)

    @Before
    override fun setup() {
        userComponent?.inject(this)
        super.setup()

        settings.login("X", "EMAIL", "TOKEN")

        val syncResponseMock = MockResponse()
            .setResponseCode(200)
        MockWebServerDispatcher.set(
            mapOf(
                "/api/realtime/sync_measurements.json" to syncResponseMock
            ),
            server
        )
    }

    @After
    override fun cleanup() {
        super.cleanup()
        database.close()
    }

    @Test
    fun testFixedOutdoorSessionRecording() {
        testRule.launchActivity(null)

        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.navigation_lets_begin), isDisplayed())).perform(click())

        onView(withId(R.id.fixed_session_start_card)).perform(click())

        onView(withId(R.id.turn_on_airbeam_ready_button)).perform(click())

        onView(withText(containsString(FakeDeviceItem.NAME.uppercase(Locale.getDefault())))).perform(
            click()
        )

        onView(withId(R.id.connect_button)).perform(click())
        awaitForAssertion {
            onView(withId(R.id.airbeam_connected_header)).check(matches(isDisplayed()))
        }
        onView(withId(R.id.airbeam_connected_continue_button)).perform(click())

        // replaceText is needed here to go around autocorrect...
        onView(withId(R.id.session_name_input)).perform(replaceText("Ania's fixed outdoor session"))
        onView(withId(R.id.session_tags_input)).perform(replaceText("tag1 tag2"))
        Espresso.closeSoftKeyboard()

        onView(withId(R.id.outdoor_button)).perform(click())

        onView(withId(R.id.cellular_button)).perform(click())
        onView(withId(R.id.networks_list_header)).check(matches(not(isDisplayed())))
        onView(withId(R.id.wifi_button)).perform(click())

        onView(withId(R.id.continue_button)).perform(scrollTo())
        onView(withId(R.id.networks_list_header)).check(matches(isDisplayed()))

        onView(withText(containsString(FakeFixedSessionDetailsController.TEST_WIFI_SSID))).perform(
            click()
        )
        onView(withId(R.id.wifi_password_input)).perform(replaceText("secret"))
        onView(withId(R.id.ok_button)).perform(click())

        onView(withId(R.id.continue_button)).perform(click())

        onView(withId(R.id.map)).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.continue_button), isDescendantOfA(withId(R.id.choose_location))))
            .perform(scrollTo(), click())

        onView(allOf(withId(R.id.map), isDescendantOfA(withId(R.id.confirmation))))
            .check(matches(isDisplayed()))

        onView(withId(R.id.start_recording_button)).perform(scrollTo(), click())

        onView(withId(R.id.tabs)).perform(selectTabAtPosition(DashboardPagerAdapter.FIXED_TAB_INDEX))


        awaitForAssertion {
            onView(
                allOf(withId(R.id.session_name), isDisplayed())
            ).check(matches(withText("Ania's fixed outdoor session")))
        }
        onView(allOf(withId(R.id.session_info), isDisplayed())).check(matches(withText("Fixed: ")))
    }

    @Test
    fun testFixedIndoorSessionRecording() {
        testRule.launchActivity(null)

        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.navigation_lets_begin), isDisplayed())).perform(click())

        onView(withId(R.id.fixed_session_start_card)).perform(click())

        onView(withId(R.id.turn_on_airbeam_ready_button)).perform(click())

        onView(withText(containsString(FakeDeviceItem.NAME.uppercase(Locale.getDefault())))).perform(
            click()
        )

        onView(withId(R.id.connect_button)).perform(click())
        awaitForAssertion {
            onView(withId(R.id.airbeam_connected_header)).check(matches(isDisplayed()))
        }
        onView(withId(R.id.airbeam_connected_continue_button)).perform(click())

        // replaceText is needed here to go around autocorrect...
        onView(withId(R.id.session_name_input)).perform(replaceText("Ania's fixed indoor session"))
        onView(withId(R.id.session_tags_input)).perform(replaceText("tag1 tag2"))
        Espresso.closeSoftKeyboard()

        onView(withId(R.id.indoor_button)).perform(click())

        onView(withId(R.id.cellular_button)).perform(click())
        onView(withId(R.id.networks_list_header)).check(matches(not(isDisplayed())))
        onView(withId(R.id.wifi_button)).perform(click())

        onView(withId(R.id.continue_button)).perform(scrollTo())
        onView(withId(R.id.networks_list_header)).check(matches(isDisplayed()))

        onView(withText(containsString(FakeFixedSessionDetailsController.TEST_WIFI_SSID))).perform(
            click()
        )
        onView(withId(R.id.wifi_password_input)).perform(replaceText("secret"))
        onView(withId(R.id.ok_button)).perform(click())

        onView(withId(R.id.continue_button)).perform(click())

        onView(withId(R.id.map)).check(matches(not(isDisplayed())))
        onView(withId(R.id.start_recording_button)).perform(scrollTo(), click())

        onView(withId(R.id.tabs)).perform(selectTabAtPosition(DashboardPagerAdapter.FIXED_TAB_INDEX))

        awaitForAssertion {
            onView(
                allOf(withId(R.id.session_name), isDisplayed())
            ).check(matches(withText("Ania's fixed indoor session")))
        }
        onView(allOf(withId(R.id.session_info), isDisplayed())).check(matches(withText("Fixed: ")))
    }


    @Ignore("Flaky")
    @Test
    fun testFollow() {
        val session = Session(
            Session.generateUUID(),
            "device_id",
            DeviceItem.Type.AIRBEAM2,
            Session.Type.FIXED,
            "New session to follow",
            ArrayList<String>(),
            Session.Status.FINISHED
        )

        testRule.launchActivity(null)

        runBlocking {
            val sessionId = sessionsRepository.insert(session)
            val streamId = measurementStreamRepository.getIdOrInsert(sessionId, stream)
            measurementsRepository.insertAll(streamId, sessionId, measurements)
        }

        onView(withId(R.id.tabs))
            .perform(selectTabAtPosition(DashboardPagerAdapter.FIXED_TAB_INDEX))

        expandCard()

        checkMeasurementTableValueIsCorrect(measurementValue)
        onView(withId(R.id.follow_button)).perform(click())
        checkMeasurementTableValueIsCorrect(measurementValue)
    }

    private fun checkMeasurementTableValueIsCorrect(measurementValue: Double) {
        awaitForAssertion {
            onView(withId(R.id.measurement_value))
                .check(matches(withText(measurementValue.roundToInt().toString())))
        }
    }
}
