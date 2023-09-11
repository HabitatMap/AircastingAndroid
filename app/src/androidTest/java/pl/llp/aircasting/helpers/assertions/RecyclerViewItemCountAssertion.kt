package pl.llp.aircasting.helpers.assertions

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.ViewAssertion

fun assertRecyclerViewItemCount(interaction: (Int) -> Boolean) =
    ViewAssertion { view, noViewFoundException ->
        if (noViewFoundException != null) {
            throw noViewFoundException
        }

        val recyclerView = view as RecyclerView
        val adapter = recyclerView.adapter
        assert(interaction(adapter?.itemCount ?: 0))
    }

fun isNotEmpty(): ViewAssertion {
    return ViewAssertion { view, noViewFoundException ->
        if (noViewFoundException != null) throw noViewFoundException
        val recyclerView = (view as? RecyclerView)
            ?: throw IllegalArgumentException("The asserted view is not a RecyclerView")

        assert(recyclerView.adapter != null && recyclerView.adapter!!.itemCount != 0) {
            "The RecyclerView adapter was empty or null"
        }
    }
}