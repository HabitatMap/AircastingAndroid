package io.lunarlogic.aircasting.screens.sync

import android.content.Context
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.settings.clear_sd_card.ClearSDCardWizardNavigator
import io.lunarlogic.aircasting.screens.sync.error.ErrorFragment
import io.lunarlogic.aircasting.screens.sync.error.ErrorViewMvc
import io.lunarlogic.aircasting.screens.sync.refreshed.RefreshedSessionsFragment
import io.lunarlogic.aircasting.screens.sync.refreshed.RefreshedSessionsViewMvc
import io.lunarlogic.aircasting.screens.sync.refreshing.RefreshingSessionsFragment
import io.lunarlogic.aircasting.screens.sync.synced.AirbeamSyncedFragment
import io.lunarlogic.aircasting.screens.sync.synced.AirbeamSyncedViewMvc
import io.lunarlogic.aircasting.screens.sync.syncing.AirbeamSyncingFragment

class SyncWizardNavigator(
    context: Context,
    settings: Settings,
    viewMvc: SyncViewMvc,
    fragmentManager: FragmentManager
): ClearSDCardWizardNavigator(
    context,
    settings,
    viewMvc,
    fragmentManager,
    R.id.airbeam_sync_fragment_container
) {
    override fun selectDeviceHeader(): String {
        return mContext.getString(R.string.airbeam_sync_select_device_header)
    }

    fun goToRefreshingSessions() {
        incrementStepProgress()
        val fragment = RefreshingSessionsFragment()
        goToFragment(fragment)
    }

    fun goToRefreshingSessionsSuccess(listener: RefreshedSessionsViewMvc.Listener) {
        goToRefreshedSessions(listener, success = true)
    }

    fun goToRefreshingSessionsError(listener: RefreshedSessionsViewMvc.Listener) {
        goToRefreshedSessions(listener, success = false)
    }

    private fun goToRefreshedSessions(listener: RefreshedSessionsViewMvc.Listener, success: Boolean) {
        incrementStepProgress()
        val fragment = RefreshedSessionsFragment(mFragmentManager)
        fragment.success = success
        fragment.listener = listener
        registerBackPressed(fragment)
        goToFragment(fragment)
    }

    fun goToAirbeamSyncing() {
        incrementStepProgress()
        val fragment = AirbeamSyncingFragment(mFragmentManager)
        registerBackPressed(fragment)
        goToFragment(fragment)
    }

    fun goToAirbeamSynced(listener: AirbeamSyncedViewMvc.Listener) {
        incrementStepProgress()
        val fragment = AirbeamSyncedFragment()
        fragment.listener = listener
        goToFragment(fragment)
    }

    fun showError(listener: ErrorViewMvc.Listener, message: String?) {
        incrementStepProgress()
        val fragment = ErrorFragment()
        fragment.listener = listener
        fragment.message = message
        goToFragment(fragment)
    }
}
