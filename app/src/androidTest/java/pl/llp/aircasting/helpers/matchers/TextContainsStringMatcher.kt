package pl.llp.aircasting.helpers.matchers

import android.view.View
import android.widget.TextView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description

class TextContainsStringMatcher(private val string: String) :
    BoundedMatcher<View, TextView>(TextView::class.java) {
    override fun describeTo(description: Description?) {
        description?.appendText("Checking the matcher on received view: with text=$string")
    }

    override fun matchesSafely(item: TextView?) =
        item?.text?.contains(string, true) ?: false
}