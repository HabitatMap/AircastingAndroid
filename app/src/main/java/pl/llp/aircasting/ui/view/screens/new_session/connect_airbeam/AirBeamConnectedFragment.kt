package pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.ui.view.common.BaseFragment
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem

class AirBeamConnectedFragment : BaseFragment<AirBeamConnectedViewMvcImpl, AirBeamConnectedController>() {

    lateinit var listener: AirBeamConnectedViewMvc.Listener
    lateinit var deviceItem: DeviceItem
    lateinit var sessionUUID: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view =
            AirBeamConnectedViewMvcImpl(
                layoutInflater,
                null,
                deviceItem,
                sessionUUID
            )
        controller =
            AirBeamConnectedController(view)

        return view?.rootView
    }

    override fun onStart() {
        super.onStart()
        listener.let { controller?.registerListener(it) }
    }

    override fun onStop() {
        super.onStop()
        listener.let { controller?.unregisterListener(it) }
    }
}
