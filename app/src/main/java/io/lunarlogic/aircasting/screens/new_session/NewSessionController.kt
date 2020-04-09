package io.lunarlogic.aircasting.screens.new_session

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.TurnAirBeamFragment
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.TurnAirBeamViewMvc
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceFragment
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceViewMvc
import io.lunarlogic.aircasting.screens.new_session.select_device.items.ADD_NEW_DEVICE_VIEW_TYPE
import io.lunarlogic.aircasting.screens.new_session.select_device.items.DeviceItem

class NewSessionController(
    private val mContext: Context?,
    private val mViewMvc: NewSessionViewMvc,
    private val mFragmentManager: FragmentManager
) : SelectDeviceViewMvc.Listener, TurnAirBeamViewMvc.Listener {

    fun onStart() {
        replaceFragment(SelectDeviceFragment(this))
    }

    override fun onDeviceItemSelected(deviceItem: DeviceItem) {
        when (deviceItem.viewType) {
            ADD_NEW_DEVICE_VIEW_TYPE -> goToTurnOnAirBeam()
        }
    }

    override fun onReadyClicked() {
        println("Ready!")
    }

    private fun goToTurnOnAirBeam() {
        goToFragment(TurnAirBeamFragment(this))
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