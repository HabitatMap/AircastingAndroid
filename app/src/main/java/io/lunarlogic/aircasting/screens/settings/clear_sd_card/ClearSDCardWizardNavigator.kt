package io.lunarlogic.aircasting.screens.settings.clear_sd_card

import android.content.Context
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.screens.common.BaseWizardNavigator
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.*
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceFragment
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceViewMvc
import io.lunarlogic.aircasting.screens.settings.clear_sd_card.sd_card_cleared.SDCardClearedFragment
import io.lunarlogic.aircasting.screens.settings.clear_sd_card.sd_card_cleared.SDCardClearedViewMvc
import io.lunarlogic.aircasting.screens.settings.clear_sd_card.clearing_sd_card.ClearingSDCardFragment
import io.lunarlogic.aircasting.screens.settings.clear_sd_card.restart_airbeam.RestartAirBeamFragment
import io.lunarlogic.aircasting.screens.settings.clear_sd_card.restart_airbeam.RestartAirBeamViewMvc

class ClearSDCardWizardNavigator(
    private val mContext: Context,
    viewMvc: ClearSDCardViewMvc,
    fragmentManager: FragmentManager
): BaseWizardNavigator(
    viewMvc,
    fragmentManager,
    R.id.clear_sd_card_fragment_container
) {
    fun goToTurnOnLocationServices(
        listener: TurnOnLocationServicesViewMvc.Listener
    ) {
        incrementStepProgress()
        val fragment = TurnOnLocationServicesFragment(useDetailedExplanation = true)
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
        fragment.headerDescription = mContext.getString(R.string.sd_card_clear_select_device_header)
        goToFragment(fragment)
    }

    fun goToClearingSDCard() {
        incrementStepProgress()
        val fragment = ClearingSDCardFragment()
        registerBackPressed(fragment)
        goToFragment(fragment)
    }

    fun goToSDCardCleared(listener: SDCardClearedViewMvc.Listener) {
        incrementStepProgress()
        val fragment = SDCardClearedFragment()
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
