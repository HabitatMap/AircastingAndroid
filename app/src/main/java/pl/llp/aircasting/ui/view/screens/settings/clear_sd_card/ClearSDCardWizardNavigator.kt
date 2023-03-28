package pl.llp.aircasting.ui.view.screens.settings.clear_sd_card

import android.content.Context
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseWizardNavigator
import pl.llp.aircasting.ui.view.fragments.ClearingSDCardFragment
import pl.llp.aircasting.ui.view.fragments.RestartAirBeamFragment
import pl.llp.aircasting.ui.view.fragments.SDCardClearedFragment
import pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam.*
import pl.llp.aircasting.ui.view.screens.new_session.select_device.SelectDeviceFragment
import pl.llp.aircasting.ui.view.screens.new_session.select_device.SelectDeviceViewMvc
import pl.llp.aircasting.ui.view.screens.settings.clear_sd_card.restart_airbeam.RestartAirBeamViewMvc
import pl.llp.aircasting.ui.view.screens.settings.clear_sd_card.sd_card_cleared.SDCardClearedViewMvc
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager

open class ClearSDCardWizardNavigator(
    protected val mContext: Context,
    viewMvc: ClearSDCardViewMvc,
    protected val mFragmentManager: FragmentManager,
    val container: Int = R.id.clear_sd_card_fragment_container,
    private val mSettings: Settings,
): BaseWizardNavigator(
    viewMvc,
    mFragmentManager,
    container
) {
    override val STEP_PROGRESS = 10

    open fun selectDeviceHeader(): String {
        return mContext.getString(R.string.sd_card_clear_select_device_header)
    }

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
        incrementStepProgress()
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
        fragment.headerDescription = selectDeviceHeader()
        goToFragment(fragment)
    }

    fun goToClearingSDCard() {
        incrementStepProgress()
        val fragment = ClearingSDCardFragment(mFragmentManager)
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
