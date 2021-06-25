package pl.llp.aircasting.screens.sync.synced

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.screens.common.BaseFragment

class AirbeamSyncedFragment: BaseFragment<AirbeamSyncedViewMvcImpl, AirbeamSyncedController>() {
    lateinit var listener: AirbeamSyncedViewMvc.Listener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .appComponent.inject(this)

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
