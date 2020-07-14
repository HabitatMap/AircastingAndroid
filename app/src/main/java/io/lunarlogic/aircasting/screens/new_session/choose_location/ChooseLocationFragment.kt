package io.lunarlogic.aircasting.screens.new_session.choose_location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.sensor.Session

class ChooseLocationFragment() : Fragment() {
    private lateinit var controller: ChooseLocationController
    lateinit var listener: ChooseLocationViewMvc.Listener
    lateinit var session: Session
    lateinit var errorHandler: ErrorHandler

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = ChooseLocationViewMvcImpl(inflater, container, childFragmentManager, session, errorHandler)
        controller = ChooseLocationController(context, view)

        return view.rootView
    }

    override fun onStart() {
        super.onStart()
        controller.registerListener(listener)
    }

    override fun onStop() {
        super.onStop()
        controller.unregisterListener(listener)
    }
}