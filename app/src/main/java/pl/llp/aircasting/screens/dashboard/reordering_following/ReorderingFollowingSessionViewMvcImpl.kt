package pl.llp.aircasting.screens.dashboard.reordering_following

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.screens.dashboard.following.FollowingSessionViewMvcImpl

class ReorderingFollowingSessionViewMvcImpl: FollowingSessionViewMvcImpl {
    private val mReorderSessionButton: ImageView
    private val mReorderSessionInProgressButton: ImageView

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup,
        supportFragmentManager: FragmentManager
    ): super(inflater, parent, supportFragmentManager) {
        mReorderSessionButton = findViewById(R.id.reorder_session_button)
        mReorderSessionInProgressButton = findViewById(R.id.reorder_inprogress_session_button)
    }

    override fun setExpandCollapseButton() {
        mExpandSessionButton.visibility = View.INVISIBLE
        mCollapseSessionButton.visibility = View.INVISIBLE
        mReorderSessionButton.visibility = View.VISIBLE
    }

}
