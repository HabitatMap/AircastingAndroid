package pl.llp.aircasting.lib

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.appbar.MaterialToolbar
import pl.llp.aircasting.R

class AppBar {
    companion object {
        private var mTopAppBar: MaterialToolbar? = null

        private var mReorderSessionsButton: ImageView? = null
        private var mFinishedReorderingSessionsButton: Button? = null

        fun setup(view: View?, rootActivity: AppCompatActivity) {
            mTopAppBar = view?.findViewById(R.id.topAppBar);
            rootActivity.setSupportActionBar(mTopAppBar)

            mTopAppBar?.setNavigationOnClickListener {
                rootActivity.onBackPressed()
            }

            mReorderSessionsButton = view?.findViewById<ImageView>(R.id.reorder_sessions_button)
            mReorderSessionsButton?.setOnClickListener {
                onReorderSessionButtonClicked()
            }

            mFinishedReorderingSessionsButton = view?.findViewById(R.id.finished_reordering_session_button)
            mFinishedReorderingSessionsButton?.setOnClickListener {
                onFinishedReorderingSessionsButtonClicked()
            }
        }

        fun destroy() {
            mTopAppBar = null
        }

        fun adjustMenuVisibility(isFollowingTab: Boolean, followingSessionsNumber: Int = 0) { // TODO: beyond "isFollowingTab" i should check if its "Dashboard" screen and if we got more than ONE followed sessions!!!
            if (isFollowingTab && followingSessionsNumber > 1) { // todo: sessionsRepository, getFollowingSessionsNumber > 1
                mTopAppBar?.findViewById<ConstraintLayout>(R.id.reorder_buttons_group)?.visibility = View.VISIBLE
            } else {
                mTopAppBar?.findViewById<ConstraintLayout>(R.id.reorder_buttons_group)?.visibility = View.INVISIBLE
            }
        }

        fun onReorderSessionButtonClicked() {
            NavigationController.goToReorderingDashboard()
            mFinishedReorderingSessionsButton?.visibility = View.VISIBLE // Todo: extract to new function <?>
            mReorderSessionsButton?.visibility = View.INVISIBLE
        }

        private fun onFinishedReorderingSessionsButtonClicked() {
            NavigationController.goToDashboard(0)
            mFinishedReorderingSessionsButton?.visibility = View.INVISIBLE // Todo: extract to new function <?>
            mReorderSessionsButton?.visibility = View.VISIBLE
        }
    }
}
