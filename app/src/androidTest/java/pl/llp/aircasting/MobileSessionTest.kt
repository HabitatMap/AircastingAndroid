package pl.llp.aircasting

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.whenever
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.di.*
import pl.llp.aircasting.helpers.*
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import pl.llp.aircasting.ui.view.screens.main.MainActivity
import okhttp3.mockwebserver.MockResponse
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import pl.llp.aircasting.di.modules.AppModule
import java.net.HttpURLConnection
import java.util.*
import javax.inject.Inject


@RunWith(AndroidJUnit4::class)
class MobileSessionTest {
    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var apiServiceFactory: ApiServiceFactory

    @Inject
    lateinit var bluetoothManager: BluetoothManager

    @Inject
    lateinit var permissionsManager: PermissionsManager

    @get:Rule
    val testRule: ActivityTestRule<MainActivity>
            = ActivityTestRule(MainActivity::class.java, false, false)

    val app = ApplicationProvider.getApplicationContext<AircastingApplication>()

    private fun setupDagger() {
        val permissionsModule =
            TestPermissionsModule()
        val testAppComponent = DaggerTestAppComponent.builder()
            .appModule(AppModule(app))
            .apiModule(TestApiModule())
            .settingsModule(TestSettingsModule())
            .permissionsModule(permissionsModule)
            .sensorsModule(
                TestSensorsModule(
                    app
                )
            )
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
        getMockWebServerFrom(apiServiceFactory).start()
    }

    @After
    fun cleanup() {
        getMockWebServerFrom(apiServiceFactory).shutdown()
    }

    @Test
    fun testBluetoothMobileSessionRecording() {
        settings.login("X", "TOKEN")

        whenever(bluetoothManager.isBluetoothEnabled()).thenReturn(true)
        whenever(permissionsManager.locationPermissionsGranted(any())).thenReturn(true)
        stubPairedDevice(bluetoothManager)

        testRule.launchActivity(null)

        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.navigation_lets_begin), isDisplayed())).perform(click())

        onView(withId(R.id.mobile_session_start_card)).perform(click())
        onView(withId(R.id.select_device_type_bluetooth_card)).perform(click())

        onView(withId(R.id.turn_on_airbeam_ready_button)).perform(click())
        onView(withText(containsString(FakeDeviceItem.NAME.uppercase(Locale.getDefault())))).perform(click())

        onView(withId(R.id.connect_button)).perform(click())
        Thread.sleep(4000)
        onView(withId(R.id.airbeam_connected_header)).check(matches(isDisplayed()))
        onView(withId(R.id.airbeam_connected_continue_button)).perform(click())

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

        expandCard()
        onView(withId(R.id.measurements_table)).check(matches(isDisplayed()))
        onView(withId(R.id.chart_container)).check(matches(isDisplayed()))

        onView(allOf(withId(R.id.recycler_sessions), isDisplayed())).perform(swipeUp())
        Thread.sleep(1000)
        openMap()
        Thread.sleep(3000)

        onView(withId(R.id.session_name)).check(matches(withText("Ania's mobile bluetooth session")))
        onView(withId(R.id.measurements_table)).check(matches(isDisplayed()))
        onView(withId(R.id.hlu)).check(matches(isDisplayed()))

        onView(isRoot()).perform(pressBack())

        expandCard()
        onView(withId(R.id.measurements_table)).check(matches(isDisplayed()))
        onView(withId(R.id.chart_container)).check(matches(isDisplayed()))

        onView(allOf(withId(R.id.recycler_sessions), isDisplayed())).perform(swipeUp())
        Thread.sleep(1000)
        openGraph()
        Thread.sleep(3000)

        onView(withId(R.id.session_name)).check(matches(withText("Ania's mobile bluetooth session")))
        onView(withId(R.id.measurements_table)).check(matches(isDisplayed()))
        onView(withId(R.id.hlu)).check(matches(isDisplayed()))

        onView(isRoot()).perform(pressBack())

        onView(allOf(withId(R.id.recycler_sessions), isDisplayed())).perform(swipeDown())
        stopSession()

        Thread.sleep(4000)

        onView(withId(R.id.session_name)).check(matches(withText("Ania's mobile bluetooth session")))
        onView(withId(R.id.session_info)).check(matches(withText("Mobile: AirBeam2")))
        expandCard()
        onView(withId(R.id.measurements_table)).check(matches(isDisplayed()))
        onView(withId(R.id.chart_container)).check(matches(not(isDisplayed())))

        onView(allOf(withId(R.id.recycler_sessions), isDisplayed())).perform(swipeUp())
        Thread.sleep(1000)
        openMap()
        Thread.sleep(3000)

        onView(withId(R.id.more_invisible_button)).perform(click())
        onView(isRoot()).perform(swipeUp())
        onView(withId(R.id.reset_button)).perform(click())
    }

    @Test
    fun testMicrophoneMobileSessionRecording() {
        val updateResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(JsonBody.build(emptyMap<String, String>()))

        MockWebServerDispatcher.set(
            mapOf(
                "/api/user/sessions/update_session.json" to updateResponse
            ),
            getFakeApiServiceFactoryFrom(apiServiceFactory).mockWebServer
        )

        settings.login("X", "TOKEN")

        whenever(permissionsManager.locationPermissionsGranted(any())).thenReturn(true)

        testRule.launchActivity(null)

        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.navigation_lets_begin), isDisplayed())).perform(click())

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
        onView(withId(R.id.session_info)).check(matches(withText("Mobile: Phone Mic")))

        onView(withId(R.id.session_actions_button)).perform(click())

        // edit session test
        onView(withId(R.id.edit_session_button)).perform(click())
        onView(withId(R.id.session_name_input)).perform(replaceText("Ania's mobile mic session"))
        onView(isRoot()).perform(swipeUp())
        onView(withId(R.id.edit_data_button)).perform(click())
        // check if name is edited:
        Thread.sleep(2000)
        onView(withId(R.id.session_name)).check(matches(withText("Ania's mobile mic session")))

        // delete session test
        onView(withId(R.id.session_actions_button)).perform(click())
        onView(withId(R.id.delete_session_button)).perform(click())

        onView(withText(R.string.delete_all_data_from_session))
            .check(matches(isNotChecked()))
            .perform(click())
            .check(matches(isChecked()))

        onView(isRoot()).perform(swipeUp())
        onView(withId(R.id.delete_streams_button)).perform(click())
        Thread.sleep(2000)
        // check if session deleted
        onView(withText("Ania's mobile mic session")).check(doesNotExist())
    }
}
