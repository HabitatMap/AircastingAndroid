package io.lunarlogic.aircasting.screens.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.common.BaseFragment
import io.lunarlogic.aircasting.screens.new_session.session_details.SessionDetailsController
import io.lunarlogic.aircasting.screens.new_session.session_details.SessionDetailsViewMvcImpl
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
        controller = SettingsController(activity, context, view, settings, childFragmentManager)

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
