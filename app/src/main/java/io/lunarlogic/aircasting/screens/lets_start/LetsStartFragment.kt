package io.lunarlogic.aircasting.screens.lets_start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.sensor.AirBeamSyncService
import javax.inject.Inject

class LetsStartFragment : Fragment() {
    private var controller: LetsStartController? = null

    @Inject
    lateinit var airBeamSyncService: AirBeamSyncService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .appComponent.inject(this)

        val view = LetsStartViewMvcImpl(layoutInflater, null, childFragmentManager)
        controller = LetsStartController(activity, view, context, airBeamSyncService)
        controller?.onCreate()

        return view.rootView
    }

    override fun onDestroy() {
        super.onDestroy()
        controller?.onDestroy()
    }
}
