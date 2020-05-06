package io.lunarlogic.aircasting.screens.new_session.select_device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.bluetooth.BluetoothManager

class SelectDeviceFragment() : Fragment() {
    private var controller: SelectDeviceController? = null
    var listener: SelectDeviceViewMvc.Listener? = null
    var bluetoothManager: BluetoothManager? = null

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
        if (bluetoothManager != null && listener != null) {
            controller =
                SelectDeviceController(
                    context,
                    view,
                    bluetoothManager!!,
                    listener!!
                )
        }

        return view.rootView
    }

    override fun onStart() {
        super.onStart()
        controller?.onStart()
    }

    override fun onStop() {
        super.onStop()
        controller?.onStop()
    }
}
