package pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.ui.view.common.BaseObservableViewMvc

class TurnOnAirBeamViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    localSessionType: LocalSession.Type
) : BaseObservableViewMvc<TurnOnAirBeamViewMvc.Listener>(), TurnOnAirBeamViewMvc {

    init {
        val layoutId = getLayoutId(localSessionType)
        this.rootView = inflater.inflate(layoutId, parent, false)
        val button = rootView?.findViewById<Button>(R.id.turn_on_airbeam_ready_button)
        button?.setOnClickListener {
            onReadyClicked()
        }
    }

    private fun onReadyClicked() {
        for (listener in listeners) {
            listener.onTurnOnAirBeamReadyClicked()
        }
    }
    
    private fun getLayoutId(localSessionType: LocalSession.Type): Int {
        return when (localSessionType) {
            LocalSession.Type.FIXED -> R.layout.fragment_turn_on_airbeam_fixed
            LocalSession.Type.MOBILE -> R.layout.fragment_turn_on_airbeam_mobile
        }
    }
}
