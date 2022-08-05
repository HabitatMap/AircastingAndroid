package pl.llp.aircasting.helpers.matchers

import android.view.View
import android.widget.TextView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description

class TextIsNumerical : BoundedMatcher<View, TextView>(TextView::class.java) {
    override fun describeTo(description: Description?) {
        description?.appendText("Checking that text view's text is numerical")
    }

    override fun matchesSafely(item: TextView?) =
        item?.text?.all { char -> char.isDigit() } ?: false
}