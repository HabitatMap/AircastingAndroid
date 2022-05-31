package pl.llp.aircasting.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.ui.view.common.BaseFragment
import pl.llp.aircasting.ui.view.screens.settings.clear_sd_card.restart_airbeam.RestartAirBeamController
import pl.llp.aircasting.ui.view.screens.settings.clear_sd_card.restart_airbeam.RestartAirBeamViewMvc
import pl.llp.aircasting.ui.view.screens.settings.clear_sd_card.restart_airbeam.RestartAirBeamViewMvcImpl

class RestartAirBeamFragment : BaseFragment<RestartAirBeamViewMvcImpl, RestartAirBeamController>() {
    var listener: RestartAirBeamViewMvc.Listener? = null
    lateinit var localSessionType: LocalSession.Type

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
