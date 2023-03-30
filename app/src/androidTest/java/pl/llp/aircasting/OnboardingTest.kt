package pl.llp.aircasting

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.di.TestApiModule
import pl.llp.aircasting.di.TestPermissionsModule
import pl.llp.aircasting.di.TestSettingsModule
import pl.llp.aircasting.di.modules.AppModule
import pl.llp.aircasting.di.modules.PermissionsModule
import pl.llp.aircasting.ui.view.screens.main.MainActivity
import pl.llp.aircasting.util.Settings
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class OnboardingTest {
    @Inject
    lateinit var settings: Settings

    @get:Rule
    val testRule: ActivityTestRule<MainActivity>
            = ActivityTestRule(MainActivity::class.java, false, false)

    private fun setupDagger() {
        val app = ApplicationProvider.getApplicationContext<AircastingApplication>()
        val permissionsModule = TestPermissionsModule()
        val testAppComponent = DaggerTestAppComponent.builder()
            .appModule(AppModule(app))
            .apiModule(TestApiModule())
            .settingsModule(TestSettingsModule())
            .permissionsModule(permissionsModule)
            .build()
        app.userDependentComponent = testAppComponent
        testAppComponent.inject(this)
    }

    @Before
    fun setup() {
        setupDagger()
    }

    @Test
    fun onboardingTest() {
        settings.onboardingNotDisplayed()
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
