package pl.llp.aircasting.util.extensions

import android.app.Activity
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.navigation.Navigation
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

fun Activity.goToDormantTab() {
    val action = MobileNavigationDirections.actionGlobalDashboard(SessionsTab.MOBILE_DORMANT.value)
    Navigation.findNavController(this, R.id.nav_host_fragment).navigate(action)
}

fun setupAppBar(activity: BaseActivity, toolbar: Toolbar) {
    activity.setSupportActionBar(toolbar)
    adjustMenuVisibility(activity)
    toolbar.setNavigationOnClickListener {
        activity.onBackPressed()
    }
    //TODO: replace this later
}