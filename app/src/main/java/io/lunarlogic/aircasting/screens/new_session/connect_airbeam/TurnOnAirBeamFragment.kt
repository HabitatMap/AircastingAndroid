package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class TurnOnAirBeamFragment(private val mListener: TurnOnAirBeamViewMvc.Listener) : Fragment() {
    private var controller: TurnOnAirBeamController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            TurnOnAirBeamViewMvcImpl(
                layoutInflater,
                null
            )
        controller =
            TurnOnAirBeamController(
                context,
                view
            )

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