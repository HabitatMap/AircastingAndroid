package io.lunarlogic.aircasting.screens.settings.clearSDCard

import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.events.AirBeamConnectionSuccessfulEvent
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.*
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceFragment
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceViewMvc
import io.lunarlogic.aircasting.screens.settings.SDCardCleared.SDCardClearedFragment
import io.lunarlogic.aircasting.screens.settings.SDCardCleared.SDCardClearedViewMvc
import io.lunarlogic.aircasting.screens.settings.clearingSDCard.ClearingSDCardFragment
import org.greenrobot.eventbus.EventBus

class ClearSDCardWizardNavigator(
    private val mViewMvc: ClearSDCardViewMvc,
    private val mFragmentManager: FragmentManager
) {
    interface BackPressedListener {
        fun onBackPressed()
    }

    private val STEP_PROGRESS = 10
    private var currentProgressStep = 0
    private var backPressedListener: BackPressedListener? = null

    fun goToSelectDevice(bluetoothManager: BluetoothManager, listener: SelectDeviceViewMvc.Listener) {
        incrementStepProgress()
        val fragment = SelectDeviceFragment()
        fragment.bluetoothManager = bluetoothManager
        fragment.listener = listener
        goToFragment(fragment)
    }

    fun goToTurnOnLocationServices(
        listener: TurnOnLocationServicesViewMvc.Listener
    ) { //todo: think- do we want to disable them later on <?>- especially if disabled mapping
        incrementStepProgress()
        val fragment = TurnOnLocationServicesFragment() //todo: problem with the constructor
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
        incrementStepProgress() // todo: check if works fine
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

    private fun registerBackPressed(listener: BackPressedListener) {
        backPressedListener = listener
    }

    fun onBackPressed() {
        decrementStepProgress()
        backPressedListener?.onBackPressed()
    }

    private fun incrementStepProgress() {  //todo: maybe i should go for some BaseWizardNavigator ??
        currentProgressStep += 1
        updateProgressBarView()
    }

    private fun decrementStepProgress() {
        currentProgressStep -= 1
        updateProgressBarView()
    }

    private fun updateProgressBarView() {
//        val progressBar = mViewMvc.rootView?.findViewById<ProgressBar>(R.id.progress_bar)
//        progressBar?.progress = currentProgressStep * STEP_PROGRESS
    }

    private fun goToFragment(fragment: Fragment) {
        val fragmentTransaction = mFragmentManager.beginTransaction()
        val container = R.id.clear_sd_card_fragment_container

        fragmentTransaction.replace(container, fragment)
        if (currentProgressStep > 1) {
            fragmentTransaction.addToBackStack(null)
        }

        fragmentTransaction.commit()
    }
}
