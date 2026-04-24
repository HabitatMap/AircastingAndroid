package pl.llp.aircasting.ui.view.screens.new_session

import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.common.BaseWizardNavigator
import pl.llp.aircasting.ui.view.screens.new_session.choose_location.ChooseLocationFragment
import pl.llp.aircasting.ui.view.screens.new_session.choose_location.ChooseLocationViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.confirmation.ConfirmationFragment
import pl.llp.aircasting.ui.view.screens.new_session.confirmation.ConfirmationViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam.*
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.ui.view.screens.new_session.select_device.SelectDeviceFragment
import pl.llp.aircasting.ui.view.screens.new_session.select_device.SelectDeviceViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.select_device_type.SelectDeviceTypeFragment
import pl.llp.aircasting.ui.view.screens.new_session.select_device_type.SelectDeviceTypeViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.session_details.SessionDetailsFragment
import pl.llp.aircasting.ui.view.screens.new_session.session_details.SessionDetailsViewMvc
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager

class NewSessionWizardNavigator(
    private val mViewMvc: NewSessionViewMvc,
    private val mFragmentManager: FragmentManager
): BaseWizardNavigator(mViewMvc, mFragmentManager, R.id.new_session_fragment_container) {
    override val STEP_PROGRESS = 10

    fun goToSelectDeviceType(listener: SelectDeviceTypeViewMvc.Listener) {
        incrementStepProgress()
        val fragment = SelectDeviceTypeFragment()
        fragment.listener = listener
        unregisterBackPressedListener()
        goToFragment(fragment)
        updateProgressBarView()
    }

    fun goToSelectDevice(bluetoothManager: BluetoothManager, listener: SelectDeviceViewMvc.Listener) {
        incrementStepProgress()
        val fragment = SelectDeviceFragment()
        fragment.bluetoothManager = bluetoothManager
        fragment.listener = listener
        unregisterBackPressedListener()
        goToFragment(fragment)
    }

    fun goToTurnOnBluetooth(listener: TurnOnBluetoothViewMvc.Listener) {
        incrementStepProgress()
        val fragment = TurnOnBluetoothFragment()
        fragment.listener = listener
        goToFragment(fragment)
    }

    fun goToTurnOnLocationServices(
        listener: TurnOnLocationServicesViewMvc.Listener,
        areMapsDisabled: Boolean,
        sessionType: Session.Type
    ) {
        incrementStepProgress()
        val useDetailedExplanation = (areMapsDisabled && sessionType == Session.Type.MOBILE)
        val fragment = TurnOnLocationServicesFragment(useDetailedExplanation, areMapsDisabled)
        fragment.listener = listener
        goToFragment(fragment)
    }

    fun goToTurnOffLocationServices(session: Session, listener: TurnOffLocationServicesViewMvc.Listener) {
        incrementStepProgress()
        val fragment = TurnOffLocationServicesFragment()
        fragment.listener = listener
        fragment.session = session
        goToFragment(fragment)
    }

    fun goToTurnOnAirBeam(sessionType: Session.Type, listener: TurnOnAirBeamViewMvc.Listener) {
        incrementStepProgress()

        val fragment = TurnOnAirBeamFragment()
        fragment.listener = listener
        fragment.sessionType = sessionType
        unregisterBackPressedListener()
        goToFragment(fragment)
    }

    fun goToConnectingAirBeam() {
        incrementStepProgress()
        val fragment = ConnectingAirBeamFragment()
        registerBackPressed(fragment)
        goToFragment(fragment)
    }

    fun goToAirBeamConnected(deviceItem: DeviceItem, sessionUUID: String, listener: AirBeamConnectedViewMvc.Listener) {
        incrementStepProgress()
        val fragment = AirBeamConnectedFragment()
        fragment.deviceItem = deviceItem
        fragment.sessionUUID = sessionUUID
        fragment.listener = listener
        unregisterBackPressedListener()
        goToFragment(fragment)
    }

    fun goToSessionDetails(sessionUUID: String, sessionType: Session.Type, deviceItem: DeviceItem, listener: SessionDetailsViewMvc.Listener) {
        incrementStepProgress()
        val fragment = SessionDetailsFragment()
        fragment.listener = listener
        fragment.deviceItem = deviceItem
        fragment.sessionUUID = sessionUUID
        fragment.sessionType = sessionType
        goToFragment(fragment)
    }

    fun goToChooseLocation(session: Session, listener: ChooseLocationViewMvc.Listener, errorHandler: ErrorHandler) {
        incrementStepProgress()
        val fragment = ChooseLocationFragment()
        fragment.session = session
        fragment.listener = listener
        fragment.errorHandler = errorHandler
        goToFragment(fragment)
    }

    fun goToConfirmation(session: Session?, listener: ConfirmationViewMvc.Listener) {
        session ?: return

        incrementStepProgress()
        val fragment = ConfirmationFragment()
        fragment.listener = listener
        fragment.session = session
        goToFragment(fragment)
    }

    fun isConnectingAirbeamFragmentVisible(): Boolean {
        for (fragment in mFragmentManager.fragments) {
            if (fragment is ConnectingAirBeamFragment) return fragment.isVisible
        }
        return false
    }
}
