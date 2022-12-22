package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.app.Activity
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.DashboardPagerAdapter.Companion.MOBILE_DORMANT_TAB_INDEX
import pl.llp.aircasting.ui.view.screens.main.MainActivity
import pl.llp.aircasting.util.Settings

interface FinishSessionListener {
    fun onFinishSessionConfirmed(session: Session) { /* Do nothing */
    }

    fun onFinishAndSyncSessionConfirmed(session: Session) { /* Do nothing */
    }
}

interface FinishMobileSessionListener : FinishSessionListener {
    val settings: Settings
    val activity: Activity?

    fun onFinishMobileSessionConfirmed() {
        settings.decreaseActiveMobileSessionsCount()

        if (settings.mobileActiveSessionsCount() < 1)
            MainActivity.navigate(
                activity,
                MOBILE_DORMANT_TAB_INDEX
            )
    }
}
