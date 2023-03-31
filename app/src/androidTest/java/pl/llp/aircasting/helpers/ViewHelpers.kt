package pl.llp.aircasting.helpers

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.awaitility.core.ThrowingRunnable
import pl.llp.aircasting.R

fun openMap() {
    clickButtonWithRetry(
        R.id.map_button,
        { onView(withId(R.id.map)).check(matches(isDisplayed())) })
}

fun openGraph() {
    clickButtonWithRetry(
        R.id.graph_button,
        { onView(withId(R.id.graph)).check(matches(isDisplayed())) })
}

fun expandCard() {
    clickButtonWithRetry(
        R.id.expand_session_button,
        { onView(withId(R.id.collapse_session_button)).check(matches(isDisplayed())) })
}

fun clickButtonWithRetry(id: Int, assertion: ThrowingRunnable, retryCount: Int = 0) {
    if (retryCount >= 3) {
        return
    }

    try {
        onView(withId(id)).perform(click())
        awaitForAssertion(assertion)
    } catch(e: Throwable) {
        clickButtonWithRetry(id, assertion, retryCount + 1)
    }
}
fun stopSession(retryCount: Int = 0) {
    if (retryCount >= 3) {
        return
    }

    try {
        onView(withId(R.id.session_actions_button)).perform(click())
        Thread.sleep(1000)
        onView(withId(R.id.stop_session_button)).perform(click())
        Thread.sleep(500)
        onView(withId(R.id.finish_recording_button)).perform(click())
    } catch(e: NoMatchingViewException) {
        stopSession(retryCount + 1)
    }
}

