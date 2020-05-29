package io.lunarlogic.aircasting

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasShortClassName
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.screens.main.MainActivity
import io.lunarlogic.aircasting.screens.new_session.LoginActivity
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Rule
import java.net.HttpURLConnection
import org.bouncycastle.crypto.tls.ConnectionEnd.server
import okhttp3.HttpUrl
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class MyRule : TestRule {
    override fun apply(base: Statement, description: Description)
            = MyStatement(base)

    class MyStatement(private val base: Statement) : Statement() {
        @Throws(Throwable::class)
        override fun evaluate() {
            val mockWebServer = MockWebServer()
            mockWebServer.start()
            val baseUrl = mockWebServer.url("/")
            ApiServiceFactory.baseUrl = baseUrl

            val mockResponse = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("{\"ania\":true}")

            mockWebServer.enqueue(mockResponse)

            // Add something you do before
            try {
                base.evaluate() // This executes your tests
            } finally {
                // Add something you do after test
            }
        }
    }
}

@RunWith(AndroidJUnit4::class)
class ConnectAirbeamInstrumentedTest {

    @get:Rule
    val myTestRule = MyRule()

    @get:Rule
    var intentsRule: IntentsTestRule<MainActivity> = IntentsTestRule(
        MainActivity::class.java)

    @Test
    fun verifySelectDeviceFlow() {



        onView(withId(R.id.login_button)).perform(click())

//        mockWebServer.shutdown()
    }
}