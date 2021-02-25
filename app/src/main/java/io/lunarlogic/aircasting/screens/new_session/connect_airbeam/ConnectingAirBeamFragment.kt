package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.screens.common.BaseWizardNavigator

class ConnectingAirBeamFragment(private val mFragmentManager: FragmentManager) : Fragment(), BaseWizardNavigator.BackPressedListener {
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

        controller = ConnectingAirBeamController(mFragmentManager)

        return view.rootView
    }

    override fun onBackPressed() {
        controller?.onBackPressed()
    }
}
