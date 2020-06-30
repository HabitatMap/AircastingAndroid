package io.lunarlogic.aircasting

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import okhttp3.mockwebserver.MockResponse
import com.google.gson.Gson
import io.lunarlogic.aircasting.screens.main.MainActivity

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Rule
import java.net.HttpURLConnection


@RunWith(AndroidJUnit4::class)
class ConnectAirbeamInstrumentedTest {
    val gson = Gson()
    val body = mapOf(
        "email" to "ania@example.org",
        "username" to "ania",
        "username" to "ania",
        "authentication_token" to "XYZ123FAKETOKEN"
    )
    val mockResponse = MockResponse()
        .setResponseCode(HttpURLConnection.HTTP_OK)
        .setBody(gson.toJson(body))

    @get:Rule
    val myTestRule = MockResponseRule(mockResponse)

    @get:Rule
    var intentsRule: IntentsTestRule<MainActivity> = IntentsTestRule(
        MainActivity::class.java)

    @Test
    fun verifySelectDeviceFlow() {
        onView(withId(R.id.login_button)).perform(click())
    }
}