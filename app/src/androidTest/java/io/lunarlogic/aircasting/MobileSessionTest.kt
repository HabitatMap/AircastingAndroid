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
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.di.*
import io.lunarlogic.aircasting.helpers.selectTabAtPosition
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.permissions.PermissionsManager
import io.lunarlogic.aircasting.screens.dashboard.DashboardPagerAdapter
import io.lunarlogic.aircasting.screens.main.MainActivity
import io.lunarlogic.aircasting.screens.new_session.select_device.items.DeviceItem
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.junit.*

import org.junit.runner.RunWith
import org.mockito.Mockito

import org.mockito.MockitoAnnotations
import javax.inject.Inject


@RunWith(AndroidJUnit4::class)
class MobileSessionTest {
    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var permissionsManager: PermissionsManager

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
    fun testMobileSessionRecording() {
        settings.setAuthToken("TOKEN")
        whenever(bluetoothManager.isBluetoothEnabled()).thenReturn(true)
        whenever(bluetoothManager.requestBluetoothPermissions()).then({})

        val deviceItem = Mockito.mock(DeviceItem::class.java)
        whenever(deviceItem.id).thenReturn("0018961070D6")
        whenever(deviceItem.name).thenReturn("AirBeam2")
        whenever(deviceItem.address).thenReturn("00:18:96:10:70:D6")
        whenever(bluetoothManager.pairedDeviceItems()).thenReturn(listOf(deviceItem))

        testRule.launchActivity(null)

        onView(withId(R.id.dashboard)).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.dashboard_record_new_session_button), isDisplayed())).perform(click())

        onView(withId(R.id.bluetooth_device_button)).perform(click())
        verify(bluetoothManager).requestBluetoothPermissions();

        onView(withId(R.id.turn_on_airbeam_ready_button)).perform(click())

        onView(withText("AirBeam2")).perform(click())

        onView(withId(R.id.connecting_airbeam_header)).check(matches(isDisplayed()))

        onView(withId(R.id.airbeam_connected_header)).check(matches(isDisplayed()))
        onView(withId(R.id.airbeam_connected_continue_button)).perform(click())

        onView(withId(R.id.session_name)).perform(typeText("Ania's session"))
        onView(withId(R.id.session_tags)).perform(typeText("tag1 tag2"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.continue_button)).perform(click())

        onView(withId(R.id.start_recording_button)).perform(click())

        Thread.sleep(2000)

        val measurementsView = onView(allOf(withId(R.id.session_measurements), isDisplayed()))
        checkMeasurement(measurementsView, "F: 1.00 F")
        checkMeasurement(measurementsView, "RH: 2.00 %")
        checkMeasurement(measurementsView,"PM1: 3.00 µg/m³");
        checkMeasurement(measurementsView,"PM2.5: 4.00 µg/m³");
        checkMeasurement(measurementsView,"PM10: 5.00 µg/m³");

        onView(allOf(withId(R.id.recycler_sessions), isDisplayed())).perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(0));
        onView(withId(R.id.stop_session_button)).perform(click())

        Thread.sleep(2000)

        onView(withId(R.id.tabs)).perform(selectTabAtPosition(DashboardPagerAdapter.MOBILE_DORMANT_TAB_INDEX))

        Thread.sleep(2000)

        val sessionNameView = onView(allOf(withId(R.id.session_name), isDisplayed()))
        sessionNameView.check(matches(withText("Ania's session")));

        val sessionTagsView = onView(allOf(withId(R.id.session_tags), isDisplayed()))
        sessionTagsView.check(matches(withText("tag1, tag2")));
    }

    private fun checkMeasurement(measurementsView: ViewInteraction, measurementString: String) {
        measurementsView.check(matches(withText(containsString(measurementString))));
    }
}