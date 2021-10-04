package pl.llp.aircasting.lib

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import pl.llp.aircasting.R

class AppBar {
    companion object {
        private var mTopAppBar: MaterialToolbar? = null

        fun setup(view: View?, rootActivity: AppCompatActivity) {
            mTopAppBar = view?.findViewById(R.id.topAppBar);
            rootActivity.setSupportActionBar(mTopAppBar)

            mTopAppBar?.setNavigationOnClickListener {
                rootActivity.onBackPressed()
            }
            mTopAppBar?.inflateMenu(R.menu.app_bar_menu)
        }

        fun destroy() {
            mTopAppBar = null
        }

        fun adjustMenuVisibility(isFollowingTab: Boolean) {
            if (isFollowingTab) {
                mTopAppBar?.menu?.setGroupVisible(0, true)
            } else {
                mTopAppBar?.menu?.setGroupVisible(0, false)
            }
        }
    }
}
