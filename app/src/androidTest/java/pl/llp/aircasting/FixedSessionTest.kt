package pl.llp.aircasting

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.whenever
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.api.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.api.repository.MeasurementsRepository
import pl.llp.aircasting.data.api.repository.SessionsRepository
import pl.llp.aircasting.di.*
import pl.llp.aircasting.di.mocks.FakeFixedSessionDetailsController
import pl.llp.aircasting.helpers.*
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import pl.llp.aircasting.ui.view.screens.dashboard.DashboardPagerAdapter
import pl.llp.aircasting.ui.view.screens.main.MainActivity
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import pl.llp.aircasting.di.modules.AppModule
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@RunWith(AndroidJUnit4::class)
class FixedSessionTest {
    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var apiServiceFactory: ApiServiceFactory

    @Inject
    lateinit var permissionsManager: PermissionsManager

    @Inject
    lateinit var bluetoothManager: BluetoothManager


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
        getMockWebServerFrom(apiServiceFactory).start()
    }

    @After
    fun cleanup() {
        getMockWebServerFrom(apiServiceFactory).shutdown()
        clearDatabase()
    }

    @Test
    fun testFixedOutdoorSessionRecording() {
        settings.login("X", "TOKEN")

        whenever(bluetoothManager.isBluetoothEnabled()).thenReturn(true)
        whenever(permissionsManager.locationPermissionsGranted(any())).thenReturn(true)
        stubPairedDevice(bluetoothManager)

        testRule.launchActivity(null)

        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.navigation_lets_begin), isDisplayed())).perform(click())

        onView(withId(R.id.fixed_session_start_card)).perform(click())

        onView(withId(R.id.turn_on_airbeam_ready_button)).perform(click())

        onView(withText(containsString(FakeDeviceItem.NAME.uppercase(Locale.getDefault())))).perform(click())

        onView(withId(R.id.connect_button)).perform(click())
        Thread.sleep(4000)
        onView(withId(R.id.airbeam_connected_header)).check(matches(isDisplayed()))
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

        onView(allOf(withId(R.id.session_name), isDisplayed())).check(matches(withText("Ania's fixed outdoor session")))
        onView(allOf(withId(R.id.session_info), isDisplayed())).check(matches(withText("Fixed: ")))
    }

    @Test
    fun testFixedIndoorSessionRecording() {
        settings.login("X", "TOKEN")

        whenever(bluetoothManager.isBluetoothEnabled()).thenReturn(true)
        whenever(permissionsManager.locationPermissionsGranted(any())).thenReturn(true)
        stubPairedDevice(bluetoothManager)

        testRule.launchActivity(null)

        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.navigation_lets_begin), isDisplayed())).perform(click())

        onView(withId(R.id.fixed_session_start_card)).perform(click())

        onView(withId(R.id.turn_on_airbeam_ready_button)).perform(click())

        onView(withText(containsString(FakeDeviceItem.NAME.uppercase(Locale.getDefault())))).perform(click())

        onView(withId(R.id.connect_button)).perform(click())
        Thread.sleep(4000)
        onView(withId(R.id.airbeam_connected_header)).check(matches(isDisplayed()))
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

        onView(withText(containsString(FakeFixedSessionDetailsController.TEST_WIFI_SSID))).perform(click())
        onView(withId(R.id.wifi_password_input)).perform(replaceText("secret"))
        onView(withId(R.id.ok_button)).perform(click())

        onView(withId(R.id.continue_button)).perform(click())

        onView(withId(R.id.map)).check(matches(not(isDisplayed())))
        onView(withId(R.id.start_recording_button)).perform(scrollTo(), click())

        Thread.sleep(4000)

        onView(withId(R.id.tabs)).perform(selectTabAtPosition(DashboardPagerAdapter.FIXED_TAB_INDEX))
        Thread.sleep(4000)

        onView(allOf(withId(R.id.session_name), isDisplayed())).check(matches(withText("Ania's fixed indoor session")))
        onView(allOf(withId(R.id.session_info), isDisplayed())).check(matches(withText("Fixed: ")))
    }

    @Test
    fun testFollowAndUnfollow() {
        settings.login("X", "TOKEN")

        val sessionsRepository = SessionsRepository()
        val measurementStreamRepository = MeasurementStreamsRepository()
        val measurementsRepository = MeasurementsRepository()

        val session = Session(
            Session.generateUUID(),
            "device_id",
            DeviceItem.Type.AIRBEAM2,
            Session.Type.FIXED,
            "New session to follow",
            ArrayList<String>(),
            Session.Status.FINISHED
        )
        val stream = MeasurementStream(
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
        val measurements = listOf(Measurement(70.0, Date()))

        DatabaseProvider.runQuery {
            val sessionId = sessionsRepository.insert(session)
            val streamId = measurementStreamRepository.getIdOrInsert(sessionId, stream)
            measurementsRepository.insertAll(streamId, sessionId, measurements)
        }

        testRule.launchActivity(null)
        onView(withId(R.id.tabs)).perform(selectTabAtPosition(DashboardPagerAdapter.FIXED_TAB_INDEX))

        expandCard()
        onView(withId(R.id.follow_button)).perform(click())
        Thread.sleep(3000)

        expandCard()
        onView(allOf(withId(R.id.recycler_sessions), isDisplayed())).perform(swipeUp())
        Thread.sleep(1000)
        onView(withId(R.id.unfollow_button)).perform(click())
        Thread.sleep(2000)
        onView(withText("New session to follow")).check(matches(isDisplayed()))
    }
}
