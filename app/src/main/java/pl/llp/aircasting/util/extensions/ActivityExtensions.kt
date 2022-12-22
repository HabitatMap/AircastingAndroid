package pl.llp.aircasting.util.extensions

import android.app.Activity
import android.view.View
import android.widget.ImageView
import androidx.navigation.Navigation
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.android.synthetic.main.prominent_app_bar.*
import pl.llp.aircasting.MobileNavigationDirections
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseActivity
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsTab

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Activity.goToFollowingTab() {
    val action = MobileNavigationDirections.actionGlobalDashboard(SessionsTab.FOLLOWING.value)
    Navigation.findNavController(this, R.id.nav_host_fragment).navigate(action)
}

fun Activity.goToMobileActiveTab() {
    val action = MobileNavigationDirections.actionGlobalDashboard(SessionsTab.MOBILE_ACTIVE.value)
    Navigation.findNavController(this, R.id.nav_host_fragment).navigate(action)
}

fun Activity.goToMobileDormantTab() {
    val action = MobileNavigationDirections.actionGlobalDashboard(SessionsTab.MOBILE_DORMANT.value)
    Navigation.findNavController(this, R.id.nav_host_fragment).navigate(action)
}

fun Activity.adjustMenuVisibility(
    isFollowingTab: Boolean = false,
    followingSessionsNumber: Int = 0
) {
    val visibility =
        if (isFollowingTab && followingSessionsNumber >= 2) View.VISIBLE else View.GONE
    topAppBar?.apply {
        findViewById<ImageView>(R.id.reorderButton)?.visibility = visibility
        findViewById<ImageView>(R.id.search_follow_icon)?.visibility =
            if (isFollowingTab) View.VISIBLE else View.INVISIBLE
    }
}

fun setupAppBar(activity: BaseActivity, toolbar: MaterialToolbar?) {
    activity.apply {
        setSupportActionBar(toolbar)
        adjustMenuVisibility()

        toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}
