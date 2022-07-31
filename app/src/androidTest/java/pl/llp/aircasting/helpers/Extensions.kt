package pl.llp.aircasting.helpers

import android.view.View
import org.awaitility.Awaitility
import org.awaitility.core.ThrowingRunnable
import org.hamcrest.Matcher
import java.util.concurrent.TimeUnit

fun hintContainsString(string: String): Matcher<in View> = HintContainsStringMatcher(string)
fun textContainsString(string: String): Matcher<in View> = TextContainsStringMatcher(string)


fun waitAndRetry(assertion: ThrowingRunnable) =
    Awaitility.await().pollDelay(1, TimeUnit.SECONDS).atMost(3, TimeUnit.SECONDS)
        .untilAsserted(assertion)
