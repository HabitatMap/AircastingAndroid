package pl.llp.aircasting.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.fragment_settings.view.*
import kotlinx.coroutines.launch
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.local.LogoutService
import pl.llp.aircasting.ui.view.common.BaseFragment
import pl.llp.aircasting.ui.view.screens.login.LoginService
import pl.llp.aircasting.ui.view.screens.settings.SettingsController
import pl.llp.aircasting.ui.view.screens.settings.SettingsViewMvcImpl
import pl.llp.aircasting.util.Settings
import javax.inject.Inject

class SettingsFragment : BaseFragment<SettingsViewMvcImpl, SettingsController>() {

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var logoutService: LogoutService

    @Inject
    lateinit var loginService: LoginService

    @Inject
    lateinit var apiServiceFactory: ApiServiceFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .userDependentComponent.inject(this)

        view = SettingsViewMvcImpl(inflater, container, settings)
        controller = SettingsController(
            activity,
            requireContext(),
            view,
            settings,
            childFragmentManager,
            loginService,
            logoutService,
            apiServiceFactory.getAuthenticated(settings.getAuthToken() ?: "")
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
