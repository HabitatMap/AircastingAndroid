package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.screens.new_session.NewSessionController
import io.lunarlogic.aircasting.screens.new_session.select_device.items.DeviceItem

class ConnectingAirBeamFragment() : Fragment(), NewSessionController.BackPressedListener {
    private var controller: ConnectingAirBeamController? = null
    var deviceItem: DeviceItem? = null
    var listener: ConnectingAirBeamController.Listener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            ConnectingAirBeamViewMvcImpl(
                layoutInflater,
                null
            )

        if (deviceItem != null && listener != null) {
            controller =
                ConnectingAirBeamController(context!!, deviceItem!!, listener!!)
        }

        return view.rootView
    }

    override fun onStart() {
        super.onStart()
        controller?.onStart()
    }

    override fun onBackPressed() {
        controller?.onBackPressed()
    }
}