package pl.llp.aircasting.ui.view.screens.sync

import android.content.Context
import android.widget.ProgressBar
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.fragments.AirbeamSyncedFragment
import pl.llp.aircasting.ui.view.fragments.AirbeamSyncingFragment
import pl.llp.aircasting.ui.view.fragments.ErrorFragment
import pl.llp.aircasting.ui.view.fragments.RefreshedSessionsFragment
import pl.llp.aircasting.ui.view.fragments.RefreshingSessionsFragment
import pl.llp.aircasting.ui.view.screens.settings.clear_sd_card.ClearSDCardWizardNavigator
import pl.llp.aircasting.ui.view.screens.sync.error.ErrorViewMvc
import pl.llp.aircasting.ui.view.screens.sync.refreshed.RefreshedSessionsViewMvc
import pl.llp.aircasting.ui.view.screens.sync.synced.AirbeamSyncedViewMvc
import pl.llp.aircasting.ui.view.screens.sync.syncing.AirbeamSyncingViewMvc
import pl.llp.aircasting.util.ProgressBarCounter
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler

class SyncWizardNavigator(
    context: Context,
    private val mViewMvc: SyncViewMvc,
    fragmentManager: FragmentManager,
    settings: Settings,
    private val errorHandler: ErrorHandler,
): ClearSDCardWizardNavigator(
    context,
    mViewMvc,
    fragmentManager,
    R.id.airbeam_sync_fragment_container,
    settings
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
        val fragment = RefreshedSessionsFragment()
        fragment.success = success
        fragment.listener = listener
        registerBackPressed(fragment)
        goToFragment(fragment)
    }

    fun goToAirbeamSyncing(listener: AirbeamSyncingViewMvc.Listener) {
        incrementStepProgress()
        val fragment = AirbeamSyncingFragment(mFragmentManager, errorHandler)
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
