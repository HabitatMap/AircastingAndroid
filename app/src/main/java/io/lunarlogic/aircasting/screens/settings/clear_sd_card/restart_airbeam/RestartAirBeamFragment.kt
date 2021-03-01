package io.lunarlogic.aircasting.screens.settings.clear_sd_card.restart_airbeam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.models.Session

class RestartAirBeamFragment() : Fragment() {
    private var controller: RestartAirBeamController? = null
    var listener: RestartAirBeamViewMvc.Listener? = null
    lateinit var sessionType: Session.Type

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            RestartAirBeamViewMvcImpl(
                layoutInflater,
                null
            )
        controller =
            RestartAirBeamController(
                context,
                view
            )

        return view.rootView
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
