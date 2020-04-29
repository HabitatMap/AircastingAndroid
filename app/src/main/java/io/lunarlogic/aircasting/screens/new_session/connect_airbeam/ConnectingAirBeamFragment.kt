package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.screens.new_session.select_device.items.DeviceItem

class ConnectingAirBeamFragment(
    private val deviceItem: DeviceItem,
    private val mListener: ConnectingAirBeamController.Listener) : Fragment() {

    private var mConnectingAirBeamController: ConnectingAirBeamController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val connectingAirBeamView =
            ConnectingAirBeamViewMvcImpl(
                layoutInflater,
                null
            )
        mConnectingAirBeamController =
            ConnectingAirBeamController(context!!, deviceItem, mListener)

        return connectingAirBeamView.rootView
    }

    override fun onStart() {
        super.onStart()
        mConnectingAirBeamController!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        mConnectingAirBeamController!!.onStop()
    }
}