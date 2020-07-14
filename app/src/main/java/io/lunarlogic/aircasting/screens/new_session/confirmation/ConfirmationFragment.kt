package io.lunarlogic.aircasting.screens.new_session.confirmation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.sensor.Session

class ConfirmationFragment() : Fragment() {
    private var controller: ConfirmationController? = null
    var listener: ConfirmationViewMvc.Listener? = null
    var session: Session? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = ConfirmationViewFactory.get(inflater, container, session!!)
        controller = ConfirmationController(context, view)

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