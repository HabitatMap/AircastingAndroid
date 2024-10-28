package pl.llp.aircasting.helpers

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import org.awaitility.Awaitility
import org.awaitility.core.ThrowingRunnable
import org.hamcrest.Matcher
import pl.llp.aircasting.helpers.matchers.HintContainsStringMatcher
import pl.llp.aircasting.helpers.matchers.TextContainsStringMatcher
import pl.llp.aircasting.helpers.matchers.TextIsNumerical
import java.util.concurrent.TimeUnit

fun hintContainsString(string: String): Matcher<in View> = HintContainsStringMatcher(string)
fun textContainsString(string: String): Matcher<in View> = TextContainsStringMatcher(string)
fun textIsNumerical(): Matcher<in View> = TextIsNumerical()

fun waitFor(delay: Long): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> = ViewMatchers.isRoot()
        override fun getDescription(): String = "wait for $delay milliseconds"
        override fun perform(uiController: UiController, v: View?) {
            uiController.loopMainThreadForAtLeast(delay)
        }
    }
}

fun waitAndRetry(assertion: ThrowingRunnable) =
    Awaitility.await()
        .pollDelay(500, TimeUnit.MILLISECONDS)
        .atMost(6, TimeUnit.SECONDS)
        .untilAsserted(assertion)

fun awaitForAssertion(assertion: ThrowingRunnable) =
    Awaitility.await()
        .atMost(8, TimeUnit.SECONDS)
        .with().pollInterval(1, TimeUnit.SECONDS)
        .pollDelay(500, TimeUnit.MILLISECONDS)
        .untilAsserted(assertion)

fun awaitUntilAsserted(assertion: ThrowingRunnable) =
    Awaitility.await()
        .atMost(30, TimeUnit.SECONDS)
        .with().pollInterval(1, TimeUnit.SECONDS)
        .pollDelay(500, TimeUnit.MILLISECONDS)
        .untilAsserted(assertion)