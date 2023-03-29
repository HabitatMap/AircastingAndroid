package pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.ui.view.common.BaseFragment
import pl.llp.aircasting.ui.view.common.BaseWizardNavigator

class ConnectingAirBeamFragment :
    BaseFragment<ConnectingAirBeamViewMvcImpl, ConnectingAirBeamController>(),
    BaseWizardNavigator.BackPressedListener {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view =
            ConnectingAirBeamViewMvcImpl(
                layoutInflater,
                null
            )

        controller = ConnectingAirBeamController(requireActivity().supportFragmentManager)

        return view?.rootView
    }

    override fun onBackPressed() {
        controller?.onBackPressed()
    }
}
