package pl.llp.aircasting.screens.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.screens.common.BaseFragment
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
