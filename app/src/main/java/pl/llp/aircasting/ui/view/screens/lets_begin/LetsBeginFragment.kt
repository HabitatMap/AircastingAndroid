package pl.llp.aircasting.ui.view.screens.lets_begin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.ui.view.screens.common.BaseFragment
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import javax.inject.Inject

class LetsBeginFragment : BaseFragment<LetsBeginViewMvcImpl, LetsBeginController>()  {

    @Inject
    lateinit var permissionsManager: PermissionsManager

    @Inject
    lateinit var errorHandler: ErrorHandler

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .appComponent.inject(this)

        val view = LetsBeginViewMvcImpl(layoutInflater, null, childFragmentManager)
        controller = LetsBeginController(activity, view, context, errorHandler)
        controller?.onCreate()

        return view.rootView
    }

}