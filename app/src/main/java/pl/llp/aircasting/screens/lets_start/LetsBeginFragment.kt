package pl.llp.aircasting.screens.lets_begin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.permissions.PermissionsManager
import pl.llp.aircasting.screens.common.BaseFragment
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