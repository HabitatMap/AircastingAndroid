package pl.llp.aircasting.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.ui.view.common.BaseFragment
import pl.llp.aircasting.ui.view.screens.sync.synced.AirbeamSyncedController
import pl.llp.aircasting.ui.view.screens.sync.synced.AirbeamSyncedViewMvc
import pl.llp.aircasting.ui.view.screens.sync.synced.AirbeamSyncedViewMvcImpl

class AirbeamSyncedFragment: BaseFragment<AirbeamSyncedViewMvcImpl, AirbeamSyncedController>() {
    lateinit var listener: AirbeamSyncedViewMvc.Listener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .userDependentComponent?.inject(this)

        view = AirbeamSyncedViewMvcImpl(layoutInflater, null)
        controller = AirbeamSyncedController(view)

        return view?.rootView
    }

    override fun onStart() {
        super.onStart()
        controller?.registerListener(listener)
    }

    override fun onStop() {
        super.onStop()
        controller?.unregisterListener(listener)
    }
}
