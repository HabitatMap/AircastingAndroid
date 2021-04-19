package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.lunarlogic.aircasting.screens.common.BaseFragment
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem

class TurnOffLocationServicesFragment:  BaseFragment<TurnOffLocationServicesViewMvcImpl, TurnOffLocationServicesController>() {
    var listener: TurnOffLocationServicesViewMvc.Listener? = null
    var deviceItem: DeviceItem? = null
    var sessionUUID: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view =
            TurnOffLocationServicesViewMvcImpl(
                layoutInflater,
                null,
                deviceItem,
                sessionUUID
            )
        controller =
            TurnOffLocationServicesController(
                requireContext(),
                view
            )

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
