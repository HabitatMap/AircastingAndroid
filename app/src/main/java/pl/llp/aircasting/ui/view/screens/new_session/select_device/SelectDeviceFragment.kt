package pl.llp.aircasting.ui.view.screens.new_session.select_device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_select_device.view.recycler_devices
import pl.llp.aircasting.R
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager


class SelectDeviceFragment : Fragment() {
    private var controller: SelectDeviceController? = null
    private var view: SelectDeviceViewMvcImpl? = null
    var listener: SelectDeviceViewMvc.Listener? = null
    var bluetoothManager: BluetoothManager? = null
    var headerDescription: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view =
            SelectDeviceViewMvcImpl(
                layoutInflater,
                null,
                headerDescription ?: getString(R.string.select_device_header)
            )

        if (listener != null) {
            controller =
                SelectDeviceController(
                    context,
                    view,
                    bluetoothManager!!,
                    listener!!
                )
        }

        return view?.rootView
    }

    override fun onStart() {
        super.onStart()
        controller?.onStart()
    }

    override fun onPause() {
        super.onPause()
        controller?.onPause()
    }

    override fun onStop() {
        super.onStop()
        controller?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        view = null
        controller?.onDestroy()
        controller = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        view = null
        controller?.onDestroy()
        controller = null
    }
}
