package io.lunarlogic.aircasting

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.whenever
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.di.*
import io.lunarlogic.aircasting.helpers.stubPairedDevice
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.permissions.PermissionsManager
import io.lunarlogic.aircasting.screens.main.MainActivity
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
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
        settings.login("X", "TOKEN")

        whenever(bluetoothManager.isBluetoothEnabled()).thenReturn(true)
        whenever(permissionsManager.locationPermissionsGranted(any())).thenReturn(true)
        val airBeamAddress = "00:18:96:10:70:D6"
        stubPairedDevice(bluetoothManager, "0018961070D6", "AirBeam2", airBeamAddress)

        testRule.launchActivity(null)

        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.navigation_lets_start), isDisplayed())).perform(click())

        onView(withId(R.id.mobile_session_start_card)).perform(click())
        onView(withId(R.id.select_device_type_bluetooth_card)).perform(click())

        onView(withId(R.id.turn_on_airbeam_ready_button)).perform(scrollTo(), click())

        onView(withText(containsString(airBeamAddress))).perform(click())

        onView(withId(R.id.connect_button)).perform(click())
        Thread.sleep(4000)
        onView(withId(R.id.airbeam_connected_header)).check(matches(isDisplayed()))
        onView(withId(R.id.airbeam_connected_header)).perform(scrollTo())
        onView(withId(R.id.airbeam_connected_continue_button)).perform(scrollTo(), click())

        // replaceText is needed here to go around autocorrect...
        onView(withId(R.id.session_name_input)).perform(replaceText("Ania's mobile bluetooth session"))
        onView(withId(R.id.session_tags_input)).perform(replaceText("tag1 tag2"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.continue_button)).perform(click())

        onView(withId(R.id.map)).check(matches(isDisplayed()))
        onView(withId(R.id.start_recording_button)).perform(scrollTo(), click())

        Thread.sleep(4000)

        val measurementValuesRow = onView(allOf(withId(R.id.measurement_values), isDisplayed()))
        measurementValuesRow.check(matches(hasMinimumChildCount(1)))

        stopSession()
        
        Thread.sleep(4000)

        onView(withId(R.id.session_name)).check(matches(withText("Ania's mobile bluetooth session")))
        onView(withId(R.id.session_info)).check(matches(withText("Mobile: AirBeam2")));
    }

    @Test
    fun testMicrophoneMobileSessionRecording() {
        settings.login("X", "TOKEN")

        whenever(permissionsManager.locationPermissionsGranted(any())).thenReturn(true)

        testRule.launchActivity(null)

        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.navigation_lets_start), isDisplayed())).perform(click())

        onView(withId(R.id.mobile_session_start_card)).perform(click())

        whenever(permissionsManager.audioPermissionsGranted(any())).thenReturn(true)
        onView(withId(R.id.select_device_type_microphone_card)).perform(click())

        // replaceText is needed here to go around autocorrect...
        onView(withId(R.id.session_name_input)).perform(replaceText("Ania's mobile microphone session"))
        onView(withId(R.id.session_tags_input)).perform(replaceText("tag1 tag2"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.continue_button)).perform(click())

        onView(withId(R.id.map)).check(matches(isDisplayed()))
        onView(withId(R.id.start_recording_button)).perform(scrollTo(), click())

        Thread.sleep(4000)

        val measurementValuesRow = onView(allOf(withId(R.id.measurement_values), isDisplayed()))
        measurementValuesRow.check(matches(hasMinimumChildCount(1)))

        stopSession()

        Thread.sleep(4000)

        onView(withId(R.id.session_name)).check(matches(withText("Ania's mobile microphone session")))
        onView(withId(R.id.session_info)).check(matches(withText("Mobile: Phone Mic")));
    }

    private fun stopSession(retryCount: Int = 0) {
        if (retryCount >= 3) {
            return
        }

        try {
            onView(withId(R.id.session_actions_button)).perform(click())
            Thread.sleep(1000)
            onView(withId(R.id.stop_session_button)).perform(click())
        } catch(e: NoMatchingViewException) {
            stopSession(retryCount + 1)
        }
    }
}
