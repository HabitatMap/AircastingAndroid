package pl.llp.aircasting.screens.sync

import android.content.Context
import android.widget.ProgressBar
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.lib.ProgressBarCounter
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.screens.settings.clear_sd_card.ClearSDCardWizardNavigator
import pl.llp.aircasting.screens.sync.error.ErrorFragment
import pl.llp.aircasting.screens.sync.error.ErrorViewMvc
import pl.llp.aircasting.screens.sync.refreshed.RefreshedSessionsFragment
import pl.llp.aircasting.screens.sync.refreshed.RefreshedSessionsViewMvc
import pl.llp.aircasting.screens.sync.refreshing.RefreshingSessionsFragment
import pl.llp.aircasting.screens.sync.synced.AirbeamSyncedFragment
import pl.llp.aircasting.screens.sync.synced.AirbeamSyncedViewMvc
import pl.llp.aircasting.screens.sync.syncing.AirbeamSyncingFragment
import pl.llp.aircasting.screens.sync.syncing.AirbeamSyncingViewMvc

class SyncWizardNavigator(
    context: Context,
    settings: Settings,
    private val mViewMvc: SyncViewMvc,
    fragmentManager: FragmentManager
): ClearSDCardWizardNavigator(
    context,
    settings,
    mViewMvc,
    fragmentManager,
    R.id.airbeam_sync_fragment_container
) {

    override fun selectDeviceHeader(): String {
        return mContext.getString(R.string.airbeam_sync_select_device_header)
    }

    override fun setupProgressBarMax(
        locationServicesAreOff: Boolean,
        areMapsDisabled: Boolean,
        isBluetoothDisabled: Boolean
    ) {
        progressBarCounter.currentProgressMax = ProgressBarCounter.DEFAULT_SYNC_STEP_NUMBER * STEP_PROGRESS
        val progressBar = mViewMvc.rootView?.findViewById<ProgressBar>(R.id.progress_bar)
        progressBar?.max = progressBarCounter.currentProgressMax

        super.setupProgressBarMax(locationServicesAreOff, areMapsDisabled, isBluetoothDisabled)
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

    fun goToAirbeamSyncing(listener: AirbeamSyncingViewMvc.Listener) {
        incrementStepProgress()
        val fragment = AirbeamSyncingFragment(mFragmentManager)
        fragment.listener = listener
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
