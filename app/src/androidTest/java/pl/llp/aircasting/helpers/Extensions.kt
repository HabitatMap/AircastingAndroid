package pl.llp.aircasting.helpers

import android.view.View
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


fun waitAndRetry(assertion: ThrowingRunnable) =
    Awaitility.await().pollDelay(500, TimeUnit.MILLISECONDS).atMost(6, TimeUnit.SECONDS)
        .untilAsserted(assertion)

fun awaitForAssertion(assertion: ThrowingRunnable) =
    Awaitility.await()
        .atMost(8, TimeUnit.SECONDS)
        .with().pollInterval(1, TimeUnit.SECONDS)
        .pollDelay(500, TimeUnit.MILLISECONDS)
        .untilAsserted(assertion)
