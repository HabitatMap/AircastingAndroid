package pl.llp.aircasting.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.ui.view.common.BaseFragment
import pl.llp.aircasting.ui.view.common.BaseWizardNavigator
import pl.llp.aircasting.ui.view.screens.settings.clear_sd_card.clearing_sd_card.ClearingSDCardController
import pl.llp.aircasting.ui.view.screens.settings.clear_sd_card.clearing_sd_card.ClearingSDCardViewMvcImpl

class ClearingSDCardFragment(
    private val mFragmentManager: FragmentManager
) : BaseFragment<ClearingSDCardViewMvcImpl, ClearingSDCardController>(),
    BaseWizardNavigator.BackPressedListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .userDependentComponent.inject(this)

        view = ClearingSDCardViewMvcImpl(layoutInflater, null)
        controller = ClearingSDCardController(mFragmentManager)

        return view?.rootView
    }

    override fun onBackPressed() {
        controller?.onBackPressed()
    }
}
