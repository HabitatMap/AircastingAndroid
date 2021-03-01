package io.lunarlogic.aircasting.screens.settings.clear_sd_card

import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.screens.common.BaseWizardNavigator
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.TurnOnBluetoothFragment
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.TurnOnBluetoothViewMvc
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.TurnOnLocationServicesFragment
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.TurnOnLocationServicesViewMvc
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceFragment
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceViewMvc
import io.lunarlogic.aircasting.screens.settings.clear_sd_card.sd_card_cleared.SDCardClearedFragment
import io.lunarlogic.aircasting.screens.settings.clear_sd_card.sd_card_cleared.SDCardClearedViewMvc
import io.lunarlogic.aircasting.screens.settings.clear_sd_card.clearing_sd_card.ClearingSDCardFragment
import io.lunarlogic.aircasting.screens.settings.clear_sd_card.restart_airbeam.RestartAirBeamFragment
import io.lunarlogic.aircasting.screens.settings.clear_sd_card.restart_airbeam.RestartAirBeamViewMvc

class ClearSDCardWizardNavigator(
    private val mViewMvc: ClearSDCardViewMvc,
    private val mFragmentManager: FragmentManager
): BaseWizardNavigator(mViewMvc, mFragmentManager, R.id.clear_sd_card_fragment_container) {
    fun goToSelectDevice(bluetoothManager: BluetoothManager, listener: SelectDeviceViewMvc.Listener) {
        incrementStepProgress()
        val fragment = SelectDeviceFragment()
        fragment.bluetoothManager = bluetoothManager
        fragment.listener = listener
        goToFragment(fragment)
    }

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

    fun goToRestartAirbeam(
        listener: RestartAirBeamViewMvc.Listener
    ) {
        incrementStepProgress()
        val fragment = RestartAirBeamFragment()
        fragment.listener = listener
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
}
