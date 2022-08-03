package pl.llp.aircasting.helpers

import android.view.View
import org.awaitility.Awaitility
import org.awaitility.core.ThrowingRunnable
import org.hamcrest.Matcher
import java.util.concurrent.TimeUnit

fun hintContainsString(string: String): Matcher<in View> = HintContainsStringMatcher(string)
fun textContainsString(string: String): Matcher<in View> = TextContainsStringMatcher(string)


fun waitAndRetry(assertion: ThrowingRunnable) =
    Awaitility.await().pollDelay(3, TimeUnit.SECONDS).atMost(9, TimeUnit.SECONDS)
        .untilAsserted(assertion)
