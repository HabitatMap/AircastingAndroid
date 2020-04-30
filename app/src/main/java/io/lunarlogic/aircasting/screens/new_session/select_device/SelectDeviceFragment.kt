package io.lunarlogic.aircasting.screens.new_session.select_device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.bluetooth.BluetoothManager

class SelectDeviceFragment(private val mListener: SelectDeviceViewMvc.Listener, private val bluetoothManager: BluetoothManager) : Fragment() {
    private var controller: SelectDeviceController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            SelectDeviceViewMvcImpl(
                layoutInflater,
                null
            )
        controller =
            SelectDeviceController(
                context,
                view,
                bluetoothManager,
                mListener
            )

        return view.rootView
    }

    override fun onStart() {
        super.onStart()
        controller!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        controller!!.onStop()
    }
}
