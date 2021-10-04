package pl.llp.aircasting.screens.lets_start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.lib.AppBar
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.permissions.PermissionsManager
import pl.llp.aircasting.screens.common.BaseFragment
import javax.inject.Inject

class LetsStartFragment : BaseFragment<LetsStartViewMvcImpl, LetsStartController>()  {

    @Inject
    lateinit var permissionsManager: PermissionsManager

    @Inject
    lateinit var errorHandler: ErrorHandler

    @Inject
    lateinit var settings: Settings

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .appComponent.inject(this)

        val view = LetsStartViewMvcImpl(layoutInflater, null, childFragmentManager, settings)
        controller = LetsStartController(activity, view, context, errorHandler)
        controller?.onCreate()

        return view.rootView
    }

    override fun onResume() {
        super.onResume()
        AppBar.adjustMenuVisibility(false)
    }
}
