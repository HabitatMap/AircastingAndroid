package pl.llp.aircasting.ui.view.screens.dashboard.reordering_following

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.dashboard.following.FollowingSessionViewMvcImpl

class ReorderingFollowingSessionViewMvcImpl: FollowingSessionViewMvcImpl {

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup,
        supportFragmentManager: FragmentManager
    ): super(inflater, parent, supportFragmentManager) {
        mReorderSessionButton = findViewById(R.id.reorder_session_button)
    }

    override fun setExpandCollapseButton() {
        mExpandSessionButton.visibility = View.INVISIBLE
        mCollapseSessionButton.visibility = View.INVISIBLE
        mReorderSessionButton.visibility = View.VISIBLE
    }
}