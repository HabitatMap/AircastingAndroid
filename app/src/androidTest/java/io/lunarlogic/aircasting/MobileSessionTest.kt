package io.lunarlogic.aircasting

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import okhttp3.mockwebserver.MockResponse
import com.google.gson.Gson
import de.mannodermaus.junit5.ActivityScenarioExtension
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.di.AppModule
import io.lunarlogic.aircasting.di.MockWebServerModule
import io.lunarlogic.aircasting.di.TestSettingsModule
import io.lunarlogic.aircasting.helpers.JsonBody
import io.lunarlogic.aircasting.helpers.MockWebServerDispatcher
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.screens.main.MainActivity
import io.mockk.every
import io.mockk.mockkConstructor
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.allOf

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.extension.RegisterExtension


import java.net.HttpURLConnection
import javax.inject.Inject


class MobileSessionTest {
    private val app = ApplicationProvider.getApplicationContext<AircastingApplication>()
    private lateinit var testAppComponent: TestAppComponent

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var mockWebServer: MockWebServer

//    @get:Rule
//    val testRule: ActivityTestRule<MainActivity>
//            = ActivityTestRule(MainActivity::class.java, false, false)

    @JvmField
    @RegisterExtension
    val scenarioExtension = ActivityScenarioExtension.launch<MainActivity>()

    @BeforeEach
    fun setup() {
        testAppComponent = DaggerTestAppComponent.builder()
            .appModule(AppModule(app))
            .settingsModule(TestSettingsModule())
            .mockWebServerModule(MockWebServerModule())
            .build()
        app.appComponent = testAppComponent
        testAppComponent.inject(this)
    }

    @AfterEach
    fun cleanup() {
        mockWebServer.shutdown()
    }

    @Test
    fun testRecordingMobileSession() {
        settings.setAuthToken("TOKEN")

        println("ANIA1")
        mockkConstructor(BluetoothManager::class)
        every { anyConstructed<BluetoothManager>().isBluetoothEnabled() } returns true

//        val scenarioExtension = ActivityScenarioExtension.launch<MainActivity>()

//        testRule.launchActivity(null)

        onView(withId(R.id.dashboard)).check(matches(isDisplayed()))

        onView(allOf(withId(R.id.dashboard_record_new_session_button), isDisplayed())).perform(click())
        onView(withId(R.id.bluetooth_device_button)).perform(click())
//
//        Thread.sleep(2000)
//        onView(with).perform(click())
    }
}