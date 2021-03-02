package io.lunarlogic.aircasting.screens.sync

import android.content.Context
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.common.BaseWizardNavigator
import io.lunarlogic.aircasting.screens.common.ViewMvc
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.*
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceFragment
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceViewMvc
import io.lunarlogic.aircasting.screens.settings.clear_sd_card.restart_airbeam.RestartAirBeamFragment
import io.lunarlogic.aircasting.screens.settings.clear_sd_card.restart_airbeam.RestartAirBeamViewMvc
import io.lunarlogic.aircasting.screens.sync.synced.AirbeamSyncedFragment
import io.lunarlogic.aircasting.screens.sync.synced.AirbeamSyncedViewMvc
import io.lunarlogic.aircasting.screens.sync.syncing.AirbeamSyncingFragment

class SyncWizardNavigator(private val mContext: Context,
                          private val mSettings: Settings,
                          mViewMvc: ViewMvc,
                          private val mFragmentManager: FragmentManager
) : BaseWizardNavigator(mViewMvc, mFragmentManager, R.id.airbeam_sync_fragment_container) {
    override val STEP_PROGRESS = 4

    fun goToTurnOnLocationServices(
        listener: TurnOnLocationServicesViewMvc.Listener
    ) {
        incrementStepProgress()
        val fragment = TurnOnLocationServicesFragment(
            useDetailedExplanation = true,
            areMapsDisabled = mSettings.areMapsDisabled()
        )
        fragment.listener = listener
        goToFragment(fragment)
    }

    fun goToTurnOnBluetooth(
        listener: TurnOnBluetoothViewMvc.Listener
    ) {
        val fragment = TurnOnBluetoothFragment()
        fragment.listener = listener
        goToFragment(fragment)
    }

    fun goToRestartAirBeam(
        listener: RestartAirBeamViewMvc.Listener
    ) {
        incrementStepProgress()
        val fragment = RestartAirBeamFragment()
        fragment.listener = listener
        goToFragment(fragment)
    }

    fun goToSelectDevice(bluetoothManager: BluetoothManager, listener: SelectDeviceViewMvc.Listener) {
        incrementStepProgress()
        val fragment = SelectDeviceFragment()
        fragment.bluetoothManager = bluetoothManager
        fragment.listener = listener
        fragment.headerDescription = mContext.getString(R.string.airbeam_sync_select_device_header)
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

    fun goToTurnOffLocationServices(listener: TurnOffLocationServicesViewMvc.Listener) {
        incrementStepProgress()
        val fragment = TurnOffLocationServicesFragment()
        fragment.listener = listener
        goToFragment(fragment)
    }
}
