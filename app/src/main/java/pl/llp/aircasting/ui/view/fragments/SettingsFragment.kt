package pl.llp.aircasting.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.ui.view.common.BaseFragment
import pl.llp.aircasting.ui.view.screens.settings.SettingsController
import pl.llp.aircasting.ui.view.screens.settings.SettingsViewMvcImpl
import pl.llp.aircasting.util.Settings
import javax.inject.Inject

class SettingsFragment : BaseFragment<SettingsViewMvcImpl, SettingsController>() {

    @Inject
    lateinit var settings: Settings

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .appComponent.inject(this)

        view = SettingsViewMvcImpl(inflater, container, settings)
        controller = SettingsController(activity, requireContext(), view, settings, childFragmentManager)

        return view?.rootView
    }

    override fun onStart() {
        super.onStart()
        controller?.onStart()
    }

    override fun onStop() {
        super.onStop()
        controller?.onStop()
    }
}
