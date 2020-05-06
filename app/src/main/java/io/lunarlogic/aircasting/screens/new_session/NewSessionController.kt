package io.lunarlogic.aircasting.screens.new_session

import android.app.Activity
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceViewMvc
import io.lunarlogic.aircasting.screens.new_session.select_device.items.DeviceItem
import io.lunarlogic.aircasting.bluetooth.BluetoothActivity
import io.lunarlogic.aircasting.bluetooth.BluetoothManager
import io.lunarlogic.aircasting.exceptions.BluetoothNotSupportedException
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.ResultCodes
import io.lunarlogic.aircasting.screens.dashboard.*
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.*
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceFragment

class NewSessionController(
    private val mContextActivity: AppCompatActivity,
    private val mActivity: BluetoothActivity,
    private val mViewMvc: NewSessionViewMvc,
    private val mFragmentManager: FragmentManager
) : SelectDeviceTypeViewMvc.Listener,
    SelectDeviceViewMvc.Listener,
    TurnOnAirBeamViewMvc.Listener,
    TurnOnBluetoothViewMvc.Listener,
    ConnectingAirBeamController.Listener,
    AirBeamConnectedViewMvc.Listener,
    SessionDetailsViewMvc.Listener,
    ConfirmationViewMvc.Listener {

    private val STEP_PROGRESS = 10
    private var currentProgressStep = 1
    private val bluetoothManager = BluetoothManager(mActivity)
    private val errorHandler = ErrorHandler(mContextActivity)

    fun onStart() {
        showFirstStep()
    }

    private fun showFirstStep() {
        val fragment = SelectDeviceTypeFragment()
        fragment.listener = this
        replaceFragment(fragment)
        updateProgressBarView()
    }

    private fun getBluetoothDeviceFragment() : Fragment {
        try {
            if (bluetoothManager.isBluetoothEnabled()) {
                val fragment = TurnOnAirBeamFragment()
                fragment.listener = this
                return fragment
            }
        } catch(exception: BluetoothNotSupportedException) {
            errorHandler.showError(exception.messageToDisplay)
        }

        val fragment = TurnOnBluetoothFragment()
        fragment.listener = this
        return fragment
    }

    fun onBackPressed() {
        decrementStepProgress()
    }

    override fun onBluetoothDeviceSelected() {
        incrementStepProgress()
        goToFragment(getBluetoothDeviceFragment())
    }

    override fun onTurnOnBluetoothOkClicked() {
        bluetoothManager.enableBluetooth()
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            ResultCodes.AIRCASTING_PERMISSIONS_REQUEST_BLUETOOTH -> {
                if (bluetoothManager.permissionsGranted(grantResults)) {
                    mActivity.requestBluetoothEnable()
                } else {
                    errorHandler.showError(R.string.errors_bluetooth_required)
                }
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int) {
        when (requestCode) {
            ResultCodes.AIRCASTING_REQUEST_BLUETOOTH_ENABLE -> {
                if (resultCode == Activity.RESULT_OK) {
                    goToTurnOnAirBeam()
                } else {
                    errorHandler.showError(R.string.errors_bluetooth_required)
                }
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun goToTurnOnAirBeam() {
        incrementStepProgress()
        val fragment = TurnOnAirBeamFragment()
        fragment.listener = this
        goToFragment(fragment)
    }

    override fun onTurnOnAirBeamReadyClicked() {
        incrementStepProgress()
        val fragment = SelectDeviceFragment()
        fragment.bluetoothManager = bluetoothManager
        fragment.listener = this
        goToFragment(fragment)
    }

    override fun onDeviceItemSelected(deviceItem: DeviceItem) {
        incrementStepProgress()
        val fragment = ConnectingAirBeamFragment()
        fragment.deviceItem = deviceItem
        fragment.listener = this
        goToFragment(fragment)
    }

    override fun onConnectionSuccessful() {
        incrementStepProgress()
        val fragment = AirBeamConnectedFragment()
        fragment.listener = this
        goToFragment(fragment)
    }

    override fun onAirBeamConnectedContinueClicked() {
        incrementStepProgress()
        val fragment = SessionDetailsFragment()
        fragment.listener = this
        goToFragment(fragment)
    }

    override fun onSessionDetailsContinueClicked(sessionName: String?, sessionTags: List<String>) {
        incrementStepProgress()
        val fragment = ConfirmationFragment()
        fragment.listener = this
        fragment.sessionName = sessionName
        goToFragment(fragment)
    }

    override fun onStartRecordingClicked() {
        mContextActivity.finish()
    }

    private fun incrementStepProgress() {
        currentProgressStep += 1
        updateProgressBarView()
    }

    private fun decrementStepProgress() {
        currentProgressStep -= 1
        updateProgressBarView()
    }

    private fun updateProgressBarView() {
        val progressBar = mViewMvc.rootView?.findViewById<ProgressBar>(R.id.progress_bar)
        progressBar?.progress = currentProgressStep * STEP_PROGRESS
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = mFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.new_session_fragment_container, fragment)
        fragmentTransaction.commit()
    }

    private fun goToFragment(fragment: Fragment) {
        val fragmentTransaction = mFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.new_session_fragment_container, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
}