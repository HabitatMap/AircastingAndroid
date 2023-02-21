package pl.llp.aircasting.ui.view.screens.new_session.connect_airbeam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.common.BaseFragment

class TurnOnAirBeamFragment : BaseFragment<TurnOnAirBeamViewMvcImpl, TurnOnAirBeamController>() {
    companion object {
        private const val SESSION_TYPE_VALUE = "session_type"
    }
    var listener: TurnOnAirBeamViewMvc.Listener? = null
    lateinit var sessionType: Session.Type

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (savedInstanceState != null) {
            sessionType = Session.Type.fromInt(savedInstanceState.getInt(SESSION_TYPE_VALUE))
        }
        view =
            TurnOnAirBeamViewMvcImpl(
                layoutInflater,
                null,
                sessionType
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SESSION_TYPE_VALUE, sessionType.value)
    }
}
