package io.lunarlogic.aircasting.screens.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class SessionDetailsFragment() : Fragment() {
    private lateinit var controller: SessionDetailsController
    lateinit var listener: SessionDetailsViewMvc.Listener
    lateinit var sessionUUID: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = SessionDetailsViewMvcImpl(inflater, container, sessionUUID)
        controller = SessionDetailsController(context, view)

        return view.rootView
    }

    override fun onStart() {
        super.onStart()
        listener.let { controller.registerListener(it) }

    }

    override fun onStop() {
        super.onStop()
        listener.let { controller.unregisterListener(it) }
    }
}