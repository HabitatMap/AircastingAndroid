package pl.llp.aircasting.helpers.assertions

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion

class RecyclerViewItemCountAssertion(private val interaction: (Int) -> Boolean) : ViewAssertion {
    override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
        if (noViewFoundException != null) {
            throw noViewFoundException;
        }

        val recyclerView = view as RecyclerView
        val adapter = recyclerView.adapter
        assert(interaction(adapter?.itemCount ?: 0))
    }

}