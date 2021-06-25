package pl.llp.aircasting.screens.settings.clear_sd_card.restart_airbeam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.screens.common.BaseFragment

class RestartAirBeamFragment() : BaseFragment<RestartAirBeamViewMvcImpl, RestartAirBeamController>() {
    var listener: RestartAirBeamViewMvc.Listener? = null
    lateinit var sessionType: Session.Type

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view =
            RestartAirBeamViewMvcImpl(
                layoutInflater,
                null
            )
        controller =
            RestartAirBeamController(
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
