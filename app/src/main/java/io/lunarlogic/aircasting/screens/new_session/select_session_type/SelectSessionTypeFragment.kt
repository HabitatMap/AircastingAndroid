package io.lunarlogic.aircasting.screens.new_session.select_session_type

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class SelectSessionTypeFragment() : Fragment() {
    private lateinit var controller: SelectSessionTypeController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = SelectSessionTypeViewMvcImpl(inflater, container)
        controller = SelectSessionTypeController(context, view)

        return view.rootView
    }

    override fun onStart() {
        super.onStart()
        controller.onStart()
    }

    override fun onStop() {
        super.onStop()
        controller.onStop()
    }
}
