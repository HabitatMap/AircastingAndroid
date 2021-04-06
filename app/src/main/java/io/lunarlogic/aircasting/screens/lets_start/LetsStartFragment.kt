package io.lunarlogic.aircasting.screens.lets_start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.permissions.PermissionsManager
import io.lunarlogic.aircasting.screens.common.BaseFragment
import io.lunarlogic.aircasting.screens.sync.synced.AirbeamSyncedController
import io.lunarlogic.aircasting.screens.sync.synced.AirbeamSyncedViewMvcImpl
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
}
