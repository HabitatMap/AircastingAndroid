package io.lunarlogic.aircasting.screens.new_session.confirmation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.lib.KeyboardHelper
import io.lunarlogic.aircasting.sensor.Session

class ConfirmationFragment() : Fragment() {
    private lateinit var controller: ConfirmationController
    lateinit var listener: ConfirmationViewMvc.Listener
    lateinit var session: Session

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = ConfirmationViewFactory.get(inflater, container, childFragmentManager, session)
        controller = ConfirmationController(context, view)
        return view.rootView
    }

    override fun onStart() {
        super.onStart()

        controller.registerListener(listener)
        controller.onStart(context)
    }

    override fun onStop() {
        super.onStop()
        controller.unregisterListener(listener)
    }
}