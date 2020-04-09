package io.lunarlogic.aircasting.screens.new_session

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceFragment
import io.lunarlogic.aircasting.screens.new_session.select_device.SelectDeviceViewMvc
import io.lunarlogic.aircasting.screens.new_session.select_device.items.DeviceItem

class NewSessionController(
    private val mContext: Context?,
    private val mViewMvc: NewSessionViewMvc,
    private val mFragmentManager: FragmentManager
) : SelectDeviceViewMvc.Listener {

    fun onStart() {
        goToSelectDevice()
    }

    private fun goToSelectDevice() {
        goToFragment(SelectDeviceFragment(this))
    }

    private fun goToFragment(fragment: Fragment) {
        val fragmentTransaction = mFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.new_session_fragment_container, fragment)
        fragmentTransaction.commit()
    }

    override fun onDeviceItemSelected(deviceItem: DeviceItem) {
        System.out.println("onDeviceSelected! " + deviceItem.title)
    }
}