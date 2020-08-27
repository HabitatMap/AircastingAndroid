package io.lunarlogic.aircasting

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.idling.CountingIdlingResource
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
            .newSessionWizardModule(TestNewSessionWizardModule())
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
        val airBeamAddress = "00:18:96:10:70:D6"
        stubPairedDevice(bluetoothManager, "0018961070D6", "AirBeam2", airBeamAddress)

        testRule.launchActivity(null)

        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.navigation_lets_start), isDisplayed())).perform(click())

        onView(withId(R.id.fixed_session_start_card)).perform(click())
        verify(bluetoothManager).requestBluetoothPermissions();

        onView(withId(R.id.turn_on_airbeam_ready_button)).perform(scrollTo(), click())

        onView(withText(containsString(airBeamAddress))).perform(click())

        onView(withId(R.id.connect_button)).perform(click())
        Thread.sleep(4000)
        onView(withId(R.id.airbeam_connected_header)).check(matches(isDisplayed()))
        onView(withId(R.id.airbeam_connected_header)).perform(scrollTo())
        onView(withId(R.id.airbeam_connected_continue_button)).perform(scrollTo(), click())

        // replaceText is needed here to go around autocorrect...
        onView(withId(R.id.session_name_input)).perform(replaceText("Ania's fixed outdoor session"))
        onView(withId(R.id.session_tags_input)).perform(replaceText("tag1 tag2"))
        Espresso.closeSoftKeyboard()

        onView(withId(R.id.outdoor_button)).perform(click())

        onView(withId(R.id.networks_list_header)).check(matches(not(isDisplayed())))
        onView(withId(R.id.wifi_button)).perform(click())

        onView(withId(R.id.continue_button)).perform(scrollTo())
        onView(withId(R.id.networks_list_header)).check(matches(isDisplayed()))

        onView(withText(containsString(FakeFixedSessionDetailsController.TEST_WIFI_SSID))).perform(click())
        onView(withId(R.id.wifi_password_input)).perform(replaceText("secret"))
        onView(withId(R.id.ok_button)).perform(click())

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

        onView(withId(R.id.session_name)).check(matches(withText("Ania's fixed outdoor session")))
        onView(withId(R.id.session_tags)).check(matches(withText("tag1, tag2")));
    }

    @Test
    fun testFixedIndoorSessionRecording() {
        settings.setAuthToken("TOKEN")
        stubBluetooth(bluetoothManager)
        val airBeamAddress = "00:18:96:10:70:D6"
        stubPairedDevice(bluetoothManager, "0018961070D6", "AirBeam2", airBeamAddress)

        testRule.launchActivity(null)

        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.navigation_lets_start), isDisplayed())).perform(click())

        onView(withId(R.id.fixed_session_start_card)).perform(click())
        verify(bluetoothManager).requestBluetoothPermissions();

        onView(withId(R.id.turn_on_airbeam_ready_button)).perform(scrollTo(), click())

        onView(withText(containsString(airBeamAddress))).perform(click())

        onView(withId(R.id.connect_button)).perform(click())
        Thread.sleep(4000)
        onView(withId(R.id.airbeam_connected_header)).check(matches(isDisplayed()))
        onView(withId(R.id.airbeam_connected_header)).perform(scrollTo())
        onView(withId(R.id.airbeam_connected_continue_button)).perform(scrollTo(), click())

        // replaceText is needed here to go around autocorrect...
        onView(withId(R.id.session_name_input)).perform(replaceText("Ania's fixed indoor session"))
        onView(withId(R.id.session_tags_input)).perform(replaceText("tag1 tag2"))
        Espresso.closeSoftKeyboard()

        onView(withId(R.id.indoor_button)).perform(click())

        onView(withId(R.id.networks_list_header)).check(matches(not(isDisplayed())))
        onView(withId(R.id.wifi_button)).perform(click())

        onView(withId(R.id.continue_button)).perform(scrollTo())
        onView(withId(R.id.networks_list_header)).check(matches(isDisplayed()))

        onView(withText(containsString(FakeFixedSessionDetailsController.TEST_WIFI_SSID))).perform(click())
        onView(withId(R.id.wifi_password_input)).perform(replaceText("secret"))
        onView(withId(R.id.ok_button)).perform(click())

        onView(withId(R.id.continue_button)).perform(click())

        onView(withId(R.id.map)).check(matches(isDisplayed()))
        onView(withId(R.id.start_recording_button)).perform(scrollTo(), click())

        Thread.sleep(4000)

        onView(withId(R.id.tabs)).perform(selectTabAtPosition(DashboardPagerAdapter.FIXED_TAB_INDEX))
        Thread.sleep(4000)

        onView(withId(R.id.session_name)).check(matches(withText("Ania's fixed indoor session")))
        onView(withId(R.id.session_tags)).check(matches(withText("tag1, tag2")));
    }
}
