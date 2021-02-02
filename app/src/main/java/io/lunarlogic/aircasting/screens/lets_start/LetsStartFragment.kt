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
import javax.inject.Inject

class LetsStartFragment : Fragment() {
    private var controller: LetsStartController? = null

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

        val view = LetsStartViewMvcImpl(layoutInflater, null, childFragmentManager)
        controller = LetsStartController(activity, view, context, permissionsManager, errorHandler)
        controller?.onCreate()

        return view.rootView
    }

    override fun onDestroy() {
        super.onDestroy()
        controller?.onDestroy()
    }
}
