package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class TurnOnAirBeamFragment(private val mListener: TurnOnAirBeamViewMvc.Listener) : Fragment() {
    private var mTurnOnAirBeamController: TurnOnAirBeamController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val turnOnAirBeamView =
            TurnOnAirBeamViewMvcImpl(
                layoutInflater,
                null
            )
        mTurnOnAirBeamController =
            TurnOnAirBeamController(
                context,
                turnOnAirBeamView
            )

        return turnOnAirBeamView.rootView
    }

    override fun onStart() {
        super.onStart()
        mTurnOnAirBeamController!!.registerListener(mListener)
    }

    override fun onStop() {
        super.onStop()
        mTurnOnAirBeamController!!.unregisterListener(mListener)
    }
}