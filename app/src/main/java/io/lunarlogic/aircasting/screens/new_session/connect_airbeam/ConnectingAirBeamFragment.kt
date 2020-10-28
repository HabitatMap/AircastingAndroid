package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.screens.new_session.NewSessionWizardNavigator

class ConnectingAirBeamFragment() : Fragment(), NewSessionWizardNavigator.BackPressedListener {
    private var controller: ConnectingAirBeamController? = null
    lateinit var listener: ConnectingAirBeamController.Listener

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

        controller = ConnectingAirBeamController(requireContext(), listener)

        return view.rootView
    }

    override fun onBackPressed() {
        controller?.onBackPressed()
    }
}
