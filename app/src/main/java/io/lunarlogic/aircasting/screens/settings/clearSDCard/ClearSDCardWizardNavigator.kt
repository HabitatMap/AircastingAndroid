package io.lunarlogic.aircasting.screens.settings.clearSDCard

import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.screens.common.BaseWizardNavigator
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.*
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceFragment
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceViewMvc
import io.lunarlogic.aircasting.screens.settings.SDCardCleared.SDCardClearedFragment
import io.lunarlogic.aircasting.screens.settings.SDCardCleared.SDCardClearedViewMvc
import io.lunarlogic.aircasting.screens.settings.clearingSDCard.ClearingSDCardFragment

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

    fun goToTurnOnAirbeam(
        listener: TurnOnAirBeamViewMvc.Listener
    ) {
        incrementStepProgress()
        val fragment = TurnOnAirBeamFragment()
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
