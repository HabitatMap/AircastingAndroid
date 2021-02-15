package io.lunarlogic.aircasting.screens.new_session.connect_airbeam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.models.Session

class TurnOnLocationServicesFragment(
    private val mAreMapsDisabled: Boolean = false,
    private val sessionType: Session.Type = Session.Type.MOBILE    //TODO: DEFAULT VALUES ADDED A BIT RANDOM FOR NOW
) : Fragment() {
    private var controller: TurnOnLocationServicesController? = null
    var listener: TurnOnLocationServicesViewMvc.Listener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            TurnOnLocationServicesViewMvcImpl(
                layoutInflater,
                null,
                mAreMapsDisabled,
                sessionType
            )
        controller =
            TurnOnLocationServicesController(
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
