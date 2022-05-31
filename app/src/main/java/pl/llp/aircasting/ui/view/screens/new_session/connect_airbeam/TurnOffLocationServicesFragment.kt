package pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.ui.view.common.BaseFragment

class TurnOffLocationServicesFragment:  BaseFragment<TurnOffLocationServicesViewMvcImpl, TurnOffLocationServicesController>() {
    var listener: TurnOffLocationServicesViewMvc.Listener? = null
    var localSession: LocalSession? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view =
            TurnOffLocationServicesViewMvcImpl(
                layoutInflater,
                null,
                localSession
            )
        controller =
            TurnOffLocationServicesController(
                requireContext(),
                view
            )

        return view?.rootView
    }

    override fun onStart() {
        super.onStart()
        listener?.let { controller?.registerListener(it) }
    }

    override fun onStop() {
        super.onStop()
        listener?.let { controller?.unregisterListener(it) }
    }
}
