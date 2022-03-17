package pl.llp.aircasting.screens.new_session.select_device_type

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.screens.common.BaseFragment
import pl.llp.aircasting.screens.new_session.select_device.SelectDeviceTypeViewMvc

class SelectDeviceTypeFragment() :
    BaseFragment<SelectDeviceTypeViewMvcImpl, SelectDeviceTypeController>() {
    var listener: SelectDeviceTypeViewMvc.Listener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = SelectDeviceTypeViewMvcImpl(inflater, container)
        controller = SelectDeviceTypeController(view)

        return view?.rootView
    }

    override fun onStart() {
        super.onStart()
        listener?.let { controller?.registerListener(it) }
    }

    override fun onStop() {
        super.onStop()
        listener?.let { controller?.unregisterListener(it) }
    }
}
