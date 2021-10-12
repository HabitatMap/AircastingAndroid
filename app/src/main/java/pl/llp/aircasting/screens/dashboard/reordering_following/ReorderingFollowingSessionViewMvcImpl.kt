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

    val mReorderSessionButton: ImageView
    val mReorderSessionInProgressButton: ImageView
    @SuppressLint("ClickableViewAccessibility")
    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup,
        supportFragmentManager: FragmentManager
    ): super(inflater, parent, supportFragmentManager) {
        // Todo: initialize the reordering "button"
        mReorderSessionButton = findViewById(R.id.reorder_session_button)
        mReorderSessionButton.setOnTouchListener(object: View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                //TODO: here i need to trigger onMove method from ItemTouchHelper/ItemTouchHelper.Callback or something ?!!?!!?
                Log.i("CARD", "onTouch session reorder button")
                return false
            }

        })
        // TODO: initialize the reordering "button" with blue halo
        mReorderSessionInProgressButton = findViewById(R.id.reorder_inprogress_session_button)
    }

    override fun setExpandCollapseButton() {
        // TODO: set collapse and expand buttons not visible
        // TODO: set reordering "button" visible in place of above mentioned buttons
        mExpandSessionButton.visibility = View.INVISIBLE
        mCollapseSessionButton.visibility = View.INVISIBLE
        mReorderSessionButton.visibility = View.VISIBLE
    }

}
