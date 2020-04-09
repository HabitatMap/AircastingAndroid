package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class TurnAirBeamFragment(private val mListener: TurnAirBeamViewMvc.Listener) : Fragment() {
    private var mTurnAirBeamController: TurnAirBeamController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val turnAirBeamView =
            TurnAirBeamViewMvcImpl(
                layoutInflater,
                null
            )
        mTurnAirBeamController =
            TurnAirBeamController(
                context,
                turnAirBeamView
            )

        return turnAirBeamView.rootView
    }

    override fun onStart() {
        super.onStart()
        mTurnAirBeamController!!.registerListener(mListener)
    }

    override fun onStop() {
        super.onStop()
        mTurnAirBeamController!!.unregisterListener(mListener)
    }
}