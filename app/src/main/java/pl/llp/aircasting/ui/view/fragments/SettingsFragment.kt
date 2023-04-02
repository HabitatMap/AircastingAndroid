package pl.llp.aircasting.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.fragment_settings.view.*
import kotlinx.coroutines.launch
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.ui.view.common.BaseFragment
import pl.llp.aircasting.ui.view.screens.settings.SettingsController
import pl.llp.aircasting.ui.view.screens.settings.SettingsControllerFactory
import pl.llp.aircasting.ui.view.screens.settings.SettingsViewMvcImpl
import pl.llp.aircasting.util.Settings
import javax.inject.Inject

class SettingsFragment : BaseFragment<SettingsViewMvcImpl, SettingsController>() {

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var controllerFactory: SettingsControllerFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .userDependentComponent?.inject(this)

        view = SettingsViewMvcImpl(inflater, container, settings)
        controller = controllerFactory.create(
            activity,
            requireContext(),
            view,
            childFragmentManager,
            lifecycleScope
        )

        return view?.rootView
    }

    override fun onStart() {
        super.onStart()
        controller?.onStart()

        lifecycleScope.launch {
            val dormantStreamAlertEnabled =
                controller?.getUserDormantStreamAlertState() ?: return@launch
            view?.rootView?.dormant_stream_alert_settings_switch?.isChecked =
                dormantStreamAlertEnabled
            controller?.saveDormantStreamAlertState(dormantStreamAlertEnabled)
        }
    }

    override fun onStop() {
        super.onStop()
        controller?.onStop()
    }
}
