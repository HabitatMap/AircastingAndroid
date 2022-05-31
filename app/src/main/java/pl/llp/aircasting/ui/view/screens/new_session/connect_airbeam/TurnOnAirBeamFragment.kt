package pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.ui.view.common.BaseFragment

class TurnOnAirBeamFragment : BaseFragment<TurnOnAirBeamViewMvcImpl, TurnOnAirBeamController>() {
    var listener: TurnOnAirBeamViewMvc.Listener? = null
    lateinit var localSessionType: LocalSession.Type

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view =
            TurnOnAirBeamViewMvcImpl(
                layoutInflater,
                null,
                localSessionType
            )
        controller =
            TurnOnAirBeamController(
                context,
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
