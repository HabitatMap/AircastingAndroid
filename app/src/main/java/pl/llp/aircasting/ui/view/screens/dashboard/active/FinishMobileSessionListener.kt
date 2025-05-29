package pl.llp.aircasting.ui.view.screens.dashboard.active

import androidx.fragment.app.FragmentActivity
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.DashboardPagerAdapter.Companion.MOBILE_DORMANT_TAB_INDEX
import pl.llp.aircasting.ui.view.screens.main.MainActivity
import pl.llp.aircasting.ui.view.screens.sync.SyncActivity
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.StopRecordingEvent

interface FinishMobileSessionListener {
    var settings: Settings
    var rootActivity: FragmentActivity

    fun onFinishMobileSessionConfirmed(session: Session) {
        val event = StopRecordingEvent(session.uuid)
        EventBus.getDefault().post(event)
        settings.decreaseActiveMobileSessionsCount()

        if (settings.mobileActiveSessionsCount() < 1)
            MainActivity.navigate(
                rootActivity,
                MOBILE_DORMANT_TAB_INDEX
            )

        if (rootActivity !is MainActivity)
            rootActivity.finish()
    }

    fun onFinishAndSyncMobileSessionConfirmed(sessionUuid: String) {
        SyncActivity.start(rootActivity, sessionUuid)
    }
}