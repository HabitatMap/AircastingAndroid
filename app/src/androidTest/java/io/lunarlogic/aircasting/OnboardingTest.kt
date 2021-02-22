package io.lunarlogic.aircasting

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import io.lunarlogic.aircasting.di.AppModule
import io.lunarlogic.aircasting.di.PermissionsModule
import io.lunarlogic.aircasting.di.TestApiModule
import io.lunarlogic.aircasting.di.TestSettingsModule
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.screens.main.MainActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class OnboardingTest {
    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var apiServiceFactory: ApiServiceFactory

    @get:Rule
    val testRule: ActivityTestRule<MainActivity>
            = ActivityTestRule(MainActivity::class.java, false, false)

    private fun setupDagger() {
        val app = ApplicationProvider.getApplicationContext<AircastingApplication>()
        val permissionsModule = PermissionsModule()
        val testAppComponent = DaggerTestAppComponent.builder()
            .appModule(AppModule(app))
            .apiModule(TestApiModule())
            .settingsModule(TestSettingsModule())
            .permissionsModule(permissionsModule)
            .build()
        app.appComponent = testAppComponent
        testAppComponent.inject(this)
    }

    @Before
    fun setup() {
        setupDagger()
    }

    @After
    fun cleanup() {

    }

    @Test
    fun onboardingTest() {
        testRule.launchActivity(null)

        onView(withId(R.id.get_started_button)).perform(click())

        onView(withId(R.id.continue_button)).perform(click())

        onView(withId(R.id.continue_button)).perform(click())

        onView(withId(R.id.accept_button)).perform(click())
        onView(withId(R.id.progress_bar)).check(matches(isDisplayed()))

        onView(withId(R.id.sign_in_button)).perform(scrollTo(), click())
        onView(withId(R.id.description)).check(matches(withText("to record and map your environment")))
    }
}
