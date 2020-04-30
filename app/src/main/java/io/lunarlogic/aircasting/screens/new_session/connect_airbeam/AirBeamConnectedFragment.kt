package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class AirBeamConnectedFragment(private val mListener: AirBeamConnectedViewMvc.Listener) : Fragment() {

    private var controller: AirBeamConnectedController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            AirBeamConnectedViewMvcImpl(
                layoutInflater,
                null
            )
        controller =
            AirBeamConnectedController(context!!, view)

        return view.rootView
    }

    override fun onStart() {
        super.onStart()
        controller!!.registerListener(mListener)
    }

    override fun onStop() {
        super.onStop()
        controller!!.unregisterListener(mListener)
    }
}