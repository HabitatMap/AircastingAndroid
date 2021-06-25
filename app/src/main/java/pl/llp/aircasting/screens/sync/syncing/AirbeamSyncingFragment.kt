package pl.llp.aircasting.screens.sync.syncing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.screens.common.BaseFragment
import pl.llp.aircasting.screens.common.BaseWizardNavigator

class AirbeamSyncingFragment(
    private val mFragmentManager: FragmentManager
) : BaseFragment<AirbeamSyncingViewMvcImpl, AirbeamSyncingController>(), BaseWizardNavigator.BackPressedListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .appComponent.inject(this)

        view = AirbeamSyncingViewMvcImpl(layoutInflater, null)
        controller = AirbeamSyncingController(view, mFragmentManager)

        controller?.onCreate()

        return view?.rootView
    }

    override fun onBackPressed() {
        controller?.onBackPressed()
    }
}
