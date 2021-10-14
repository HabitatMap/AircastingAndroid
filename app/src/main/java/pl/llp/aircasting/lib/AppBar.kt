package pl.llp.aircasting.lib

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

        fun adjustMenuVisibility(isFollowingTab: Boolean, followingSessionsNumber: Int = 0) {
            if (isFollowingTab && followingSessionsNumber > 1) {
                mTopAppBar?.findViewById<ConstraintLayout>(R.id.reorder_buttons_group)?.visibility = View.VISIBLE
            } else {
                mTopAppBar?.findViewById<ConstraintLayout>(R.id.reorder_buttons_group)?.visibility = View.INVISIBLE
            }
        }

        private fun onReorderSessionButtonClicked() {
            NavigationController.goToReorderingDashboard()
            showReorderSessionsButton()
        }

        private fun onFinishedReorderingSessionsButtonClicked() {
            NavigationController.goToDashboard(0)
            showFinishedReorderingSessionsButtonClicked()
        }

        private fun showReorderSessionsButton() {
            mFinishedReorderingSessionsButton?.visibility = View.VISIBLE
            mReorderSessionsButton?.visibility = View.INVISIBLE
        }

        private fun showFinishedReorderingSessionsButtonClicked() {
            mFinishedReorderingSessionsButton?.visibility = View.INVISIBLE
            mReorderSessionsButton?.visibility = View.VISIBLE
        }
    }
}
