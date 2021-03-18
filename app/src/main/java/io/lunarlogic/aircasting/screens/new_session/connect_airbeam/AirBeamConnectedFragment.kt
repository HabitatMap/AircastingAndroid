package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem

class AirBeamConnectedFragment() : Fragment() {

    private var controller: AirBeamConnectedController? = null
    lateinit var listener: AirBeamConnectedViewMvc.Listener
    private var view: AirBeamConnectedViewMvcImpl? = null
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
