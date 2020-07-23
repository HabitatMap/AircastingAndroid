package io.lunarlogic.aircasting

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.nhaarman.mockito_kotlin.*
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.di.*
import io.lunarlogic.aircasting.helpers.selectTabAtPosition
import io.lunarlogic.aircasting.helpers.stubBluetooth
import io.lunarlogic.aircasting.helpers.stubPairedDevice
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.permissions.PermissionsManager
import io.lunarlogic.aircasting.screens.dashboard.DashboardPagerAdapter
import io.lunarlogic.aircasting.screens.main.MainActivity
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.*
import org.junit.*

import org.junit.runner.RunWith

import org.mockito.MockitoAnnotations
import javax.inject.Inject


@RunWith(AndroidJUnit4::class)
class MobileSessionTest {
    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var bluetoothManager: BluetoothManager

    @Inject
    lateinit var permissionsManager: PermissionsManager

    @Inject
    lateinit var mockWebServer: MockWebServer

    @get:Rule
    val testRule: ActivityTestRule<MainActivity>
            = ActivityTestRule(MainActivity::class.java, false, false)

    val app = ApplicationProvider.getApplicationContext<AircastingApplication>()

    private fun setupDagger() {
        val permissionsModule = TestPermissionsModule()
        val testAppComponent = DaggerTestAppComponent.builder()
            .appModule(AppModule(app))
            .settingsModule(TestSettingsModule())
            .permissionsModule(permissionsModule)
            .sensorsModule(TestSensorsModule(app))
            .mockWebServerModule(MockWebServerModule())
            .build()
        app.appComponent = testAppComponent
        testAppComponent.inject(this)
    }

    fun clearDatabase() {
        DatabaseProvider.setup(app)
        DatabaseProvider.runQuery { DatabaseProvider.get().clearAllTables() }
    }

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        setupDagger()
        clearDatabase()
    }

    @After
    fun cleanup() {
        mockWebServer.shutdown()
    }

    @Test
    fun testBluetoothMobileSessionRecording() {
        settings.setAuthToken("TOKEN")
        stubBluetooth(bluetoothManager)
        stubPairedDevice(bluetoothManager, "0018961070D6", "AirBeam2", "00:18:96:10:70:D6")

        testRule.launchActivity(null)

        onView(withId(R.id.dashboard)).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.dashboard_record_new_session_button), isDisplayed())).perform(click())

        onView(withId(R.id.mobile_session_button)).perform(click())

        onView(withId(R.id.bluetooth_device_button)).perform(click())
        verify(bluetoothManager).requestBluetoothPermissions();

        onView(withId(R.id.turn_on_airbeam_ready_button)).perform(click())

        onView(withText("AirBeam2")).perform(click())

        onView(withId(R.id.connecting_airbeam_header)).check(matches(isDisplayed()))

        onView(withId(R.id.airbeam_connected_header)).check(matches(isDisplayed()))
        onView(withId(R.id.airbeam_connected_continue_button)).perform(click())

        onView(withId(R.id.session_name)).perform(replaceText("Ania's mobile bluetooth session"))
        onView(withId(R.id.session_tags)).perform(replaceText("tag1 tag2"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.continue_button)).perform(click())

        onView(withId(R.id.map)).check(matches(isDisplayed()))
        onView(withId(R.id.start_recording_button)).perform(scrollTo(), click())

        Thread.sleep(4000)

        val measurementsView = onView(allOf(withId(R.id.session_measurements), isDisplayed()))
        measurementsView.check(matches(not(withText(""))))

        onView(allOf(withId(R.id.recycler_sessions), isDisplayed()))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(0));
        onView(withId(R.id.stop_session_button)).perform(click())

        Thread.sleep(4000)

        onView(withId(R.id.dormant_session_name)).check(matches(withText("Ania's mobile bluetooth session")))
        onView(withId(R.id.dormant_session_tags)).check(matches(withText("tag1, tag2")));
    }

    @Test
    fun testMicrophoneMobileSessionRecording() {
        settings.setAuthToken("TOKEN")

        testRule.launchActivity(null)

        onView(withId(R.id.dashboard)).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.dashboard_record_new_session_button), isDisplayed())).perform(click())

        onView(withId(R.id.mobile_session_button)).perform(click())

        whenever(permissionsManager.audioPermissionsGranted(any())).thenReturn(true)
        onView(withId(R.id.microphone_button)).perform(click())

        onView(withId(R.id.session_name)).perform(replaceText("Ania's mobile microphone session"))
        onView(withId(R.id.session_tags)).perform(replaceText("tag1 tag2"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.continue_button)).perform(click())

        onView(withId(R.id.map)).check(matches(isDisplayed()))
        onView(withId(R.id.start_recording_button)).perform(scrollTo(), click())

        Thread.sleep(4000)

        val measurementsView = onView(allOf(withId(R.id.session_measurements), isDisplayed()))
        checkMeasurement(measurementsView, "Phone Microphone: 89.43 dB")

        onView(allOf(withId(R.id.recycler_sessions), isDisplayed()))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(0));
        onView(withId(R.id.stop_session_button)).perform(click())

        Thread.sleep(4000)

        onView(withId(R.id.dormant_session_name)).check(matches(withText("Ania's mobile microphone session")))
        onView(withId(R.id.dormant_session_tags)).check(matches(withText("tag1, tag2")));
    }

    private fun checkMeasurement(measurementsView: ViewInteraction, measurementString: String) {
        measurementsView.check(matches(withText(containsString(measurementString))));
    }
}