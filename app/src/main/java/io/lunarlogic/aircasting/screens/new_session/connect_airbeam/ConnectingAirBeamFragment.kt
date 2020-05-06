package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.screens.new_session.select_device.items.DeviceItem

class ConnectingAirBeamFragment(
    private val deviceItem: DeviceItem,
    private val mListener: ConnectingAirBeamController.Listener
) : Fragment() {

    private var controller: ConnectingAirBeamController? = null

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
        controller =
            ConnectingAirBeamController(context!!, deviceItem, mListener)

        return view.rootView
    }

    override fun onStart() {
        super.onStart()
        controller!!.onStart()
    }
}