package io.lunarlogic.aircasting.screens.new_session.select_device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.bluetooth.BluetoothManager

class SelectDeviceFragment(private val mListener: SelectDeviceViewMvc.Listener, private val bluetoothManager: BluetoothManager) : Fragment() {
    private var mSelectDeviceController: SelectDeviceController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val selectDeviceView =
            SelectDeviceViewMvcImpl(
                layoutInflater,
                null
            )
        mSelectDeviceController =
            SelectDeviceController(
                context,
                selectDeviceView,
                bluetoothManager,
                mListener
            )

        return selectDeviceView.rootView
    }

    override fun onStart() {
        super.onStart()
        mSelectDeviceController!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        mSelectDeviceController!!.onStop()
    }
}
