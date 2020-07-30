package io.lunarlogic.aircasting

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.nhaarman.mockito_kotlin.verify
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.di.*
import io.lunarlogic.aircasting.helpers.selectTabAtPosition
import io.lunarlogic.aircasting.helpers.stubBluetooth
import io.lunarlogic.aircasting.helpers.stubPairedDevice
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.dashboard.DashboardPagerAdapter
import io.lunarlogic.aircasting.screens.main.MainActivity
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.*
import org.junit.*

import org.junit.runner.RunWith

import org.mockito.MockitoAnnotations
import javax.inject.Inject


@RunWith(AndroidJUnit4::class)
class FixedSessionTest {
    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var bluetoothManager: BluetoothManager

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
    fun testFixedOutdoorSessionRecording() {
        settings.setAuthToken("TOKEN")
        stubBluetooth(bluetoothManager)
        stubPairedDevice(bluetoothManager, "0018961070D6", "AirBeam2", "00:18:96:10:70:D6")

        testRule.launchActivity(null)

        onView(withId(R.id.dashboard)).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.dashboard_record_new_session_button), isDisplayed())).perform(click())

        onView(withId(R.id.fixed_session_start_card)).perform(click())
        verify(bluetoothManager).requestBluetoothPermissions();

        onView(withId(R.id.turn_on_airbeam_ready_button)).perform(click())

        onView(withText("AirBeam2")).perform(click())

        onView(withId(R.id.connecting_airbeam_header)).check(matches(isDisplayed()))

        onView(withId(R.id.airbeam_connected_header)).check(matches(isDisplayed()))
        onView(withId(R.id.airbeam_connected_continue_button)).perform(click())

        onView(withId(R.id.session_name)).perform(replaceText("Ania's fixed outdoor session"))
        onView(withId(R.id.session_tags)).perform(replaceText("tag1 tag2"))
        Espresso.closeSoftKeyboard()

        // change to outdoor
        onView(withId(R.id.indoor_toggle)).perform(click())

        onView(withId(R.id.wifi_credentials)).check(matches(not(isDisplayed())))
        onView(withId(R.id.streaming_method_toggle)).perform(click())

        onView(withId(R.id.wifi_credentials)).check(matches(isDisplayed()))
        onView(withId(R.id.continue_button)).perform(scrollTo())

        onView(withId(R.id.wifi_name)).perform(replaceText("WIFI-SSID"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.wifi_password)).perform(replaceText("secret"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.continue_button)).perform(click())

        onView(withId(R.id.map)).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.continue_button), isDescendantOfA(withId(R.id.choose_location))))
            .perform(scrollTo(), click())

        onView(allOf(withId(R.id.map), isDescendantOfA(withId(R.id.confirmation))))
            .check(matches(isDisplayed()))

        onView(withId(R.id.start_recording_button)).perform(scrollTo(), click())

        Thread.sleep(4000)

        onView(withId(R.id.tabs)).perform(selectTabAtPosition(DashboardPagerAdapter.FIXED_TAB_INDEX))
        Thread.sleep(4000)

        onView(withId(R.id.dormant_session_name)).check(matches(withText("Ania's fixed outdoor session")))
        onView(withId(R.id.dormant_session_tags)).check(matches(withText("tag1, tag2")));
    }

    @Test
    fun testFixedIndoorSessionRecording() {
        settings.setAuthToken("TOKEN")
        stubBluetooth(bluetoothManager)
        stubPairedDevice(bluetoothManager, "0018961070D6", "AirBeam2", "00:18:96:10:70:D6")

        testRule.launchActivity(null)

        onView(withId(R.id.dashboard)).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.dashboard_record_new_session_button), isDisplayed())).perform(click())

        onView(withId(R.id.fixed_session_start_card)).perform(click())
        verify(bluetoothManager).requestBluetoothPermissions();

        onView(withId(R.id.turn_on_airbeam_ready_button)).perform(click())

        onView(withText("AirBeam2")).perform(click())

        onView(withId(R.id.connecting_airbeam_header)).check(matches(isDisplayed()))

        onView(withId(R.id.airbeam_connected_header)).check(matches(isDisplayed()))
        onView(withId(R.id.airbeam_connected_continue_button)).perform(click())

        onView(withId(R.id.session_name)).perform(replaceText("Ania's fixed indoor session"))
        onView(withId(R.id.session_tags)).perform(replaceText("tag1 tag2"))
        Espresso.closeSoftKeyboard()

        // not touching indoor_toogle - default is indoor

        onView(withId(R.id.wifi_credentials)).check(matches(not(isDisplayed())))
        onView(withId(R.id.continue_button)).perform(scrollTo())

        onView(withId(R.id.streaming_method_toggle)).perform(click())
        onView(withId(R.id.wifi_credentials)).check(matches(isDisplayed()))
        onView(withId(R.id.wifi_name)).perform(replaceText("WIFI-SSID"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.wifi_password)).perform(replaceText("secret"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.continue_button)).perform(click())

        onView(withId(R.id.map)).check(matches(isDisplayed()))
        onView(withId(R.id.start_recording_button)).perform(scrollTo(), click())

        Thread.sleep(4000)

        onView(withId(R.id.tabs)).perform(selectTabAtPosition(DashboardPagerAdapter.FIXED_TAB_INDEX))
        Thread.sleep(4000)

        onView(withId(R.id.dormant_session_name)).check(matches(withText("Ania's fixed indoor session")))
        onView(withId(R.id.dormant_session_tags)).check(matches(withText("tag1, tag2")));
    }


    private fun checkMeasurement(measurementsView: ViewInteraction, measurementString: String) {
        measurementsView.check(matches(withText(containsString(measurementString))));
    }
}
