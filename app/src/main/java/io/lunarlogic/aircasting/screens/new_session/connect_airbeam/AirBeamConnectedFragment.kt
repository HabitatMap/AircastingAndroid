package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class AirBeamConnectedFragment() : Fragment() {

    private lateinit var controller: AirBeamConnectedController
    lateinit var listener: AirBeamConnectedViewMvc.Listener
    lateinit var deviceId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            AirBeamConnectedViewMvcImpl(
                layoutInflater,
                null,
                deviceId
            )
        controller =
            AirBeamConnectedController(context!!, view)

        return view.rootView
    }

    override fun onStart() {
        super.onStart()
        listener.let { controller.registerListener(it) }
    }

    override fun onStop() {
        super.onStop()
        listener.let { controller.unregisterListener(it) }
    }
}