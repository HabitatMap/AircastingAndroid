package io.lunarlogic.aircasting.screens.sync

import android.content.Context
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.settings.clear_sd_card.ClearSDCardWizardNavigator
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
}
