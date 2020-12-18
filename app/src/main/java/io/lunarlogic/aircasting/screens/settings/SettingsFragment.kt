package io.lunarlogic.aircasting.screens.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.lib.Settings
import javax.inject.Inject

class SettingsFragment : Fragment() {

    private var controller : SettingsController? = null

    @Inject
    lateinit var settings: Settings

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = SettingsViewMvcImpl(inflater, container, childFragmentManager)

        (activity?.application as AircastingApplication)
            .appComponent.inject(this)

        controller = SettingsController(context, view, settings, childFragmentManager)

        return view.rootView
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