package io.lunarlogic.aircasting.screens.new_session.choose_location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class ChooseLocationFragment() : Fragment() {
    private lateinit var controller: ChooseLocationController
    lateinit var listener: ChooseLocationViewMvc.Listener
    lateinit var deviceId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = ChooseLocationViewMvcImpl(inflater, container, childFragmentManager)
        controller = ChooseLocationController(context, view)

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