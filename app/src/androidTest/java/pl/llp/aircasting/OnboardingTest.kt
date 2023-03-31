package pl.llp.aircasting

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pl.llp.aircasting.ui.view.screens.main.MainActivity
import pl.llp.aircasting.util.Settings
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class OnboardingTest : BaseTest() {
    @Inject
    lateinit var settings: Settings

    @get:Rule
    val testRule: ActivityTestRule<MainActivity>
            = ActivityTestRule(MainActivity::class.java, false, false)

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
