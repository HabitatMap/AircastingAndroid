package pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam

import android.content.Context
import pl.llp.aircasting.ui.view.common.BaseController

class TurnOnAirBeamController(
    private val mContext: Context?,
    viewMvc: TurnOnAirBeamViewMvcImpl?
) : BaseController<TurnOnAirBeamViewMvcImpl>(viewMvc)  {

    fun registerListener(listener: TurnOnAirBeamViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: TurnOnAirBeamViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }
}
