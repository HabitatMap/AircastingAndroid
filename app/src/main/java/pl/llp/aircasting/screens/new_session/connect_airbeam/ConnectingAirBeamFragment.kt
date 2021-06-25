package pl.llp.aircasting.screens.new_session.connect_airbeam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.screens.common.BaseFragment
import pl.llp.aircasting.screens.common.BaseWizardNavigator

class ConnectingAirBeamFragment(private val mFragmentManager: FragmentManager) : BaseFragment<ConnectingAirBeamViewMvcImpl, ConnectingAirBeamController>(), BaseWizardNavigator.BackPressedListener {
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

        controller = ConnectingAirBeamController(mFragmentManager)

        return view?.rootView
    }

    override fun onBackPressed() {
        controller?.onBackPressed()
    }
}
