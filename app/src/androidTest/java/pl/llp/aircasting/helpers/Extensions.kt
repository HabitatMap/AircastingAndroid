package pl.llp.aircasting.helpers

import android.view.View
import org.awaitility.Awaitility
import org.awaitility.core.ThrowingRunnable
import org.hamcrest.Matcher
import java.util.concurrent.TimeUnit

fun hintContainsString(string: String): Matcher<in View> = HintContainsStringMatcher(string)
fun textContainsString(string: String): Matcher<in View> = TextContainsStringMatcher(string)


fun waitAndRetry(assertion: ThrowingRunnable) =
    Awaitility.await().pollDelay(500, TimeUnit.MILLISECONDS).atMost(6, TimeUnit.SECONDS)
        .untilAsserted(assertion)

fun awaitForCondition(assertion: ThrowingRunnable) =
    Awaitility.await()
        .atLeast(2, TimeUnit.SECONDS)
        .atMost(8, TimeUnit.SECONDS)
        .with().pollInterval(1, TimeUnit.SECONDS)
        .untilAsserted(assertion)
